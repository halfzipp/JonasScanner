package com.jonasSoftware.blueharvest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

/**
 * @author Brad Bass
 * @version 1.0
 * 
 * HomeActivity allows user to either go to the ConfigActivity or the ChargeActivity.
 *
 * @see android.os
 * @see android.app
 * @see android.content
 * @see android.view
 * @see android.widget
 */

public class HomeActivity extends Activity {

    private static Button _uploadBtn;
    private static Button _chrgBtn;
    private static Button _transferBtn;
    private static Button _receiveBtn;
    private static ImageButton _chargeSendAllBtn;
    private static ImageButton _chargeDeleteAllBtn;
    private static ImageButton _uploadSendAllBtn;
    private static ImageButton _uploadDeleteAllBtn;
    private static ImageButton _transferSendAllBtn;
    private static ImageButton _transferDeleteAllBtn;
    private static ImageButton _receiveSendAllBtn;
    private static ImageButton _receiveDeleteAllBtn;
    private static String _filename;
    private static Integer _tableNum;
    private final Crypter crypter = new Crypter();
    //private Boolean save = false;

    private static DatabaseHandler _dbh;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
        setTitle(" Inventory Scanner");

        //buttons
        //final Button configBtn = (Button) findViewById(R.id.configBtn);
        //final Button settingsBtn = (Button) findViewById(R.id.settingsBtn);
        _chrgBtn = (Button) findViewById(R.id.chargeBtn);
        _uploadBtn = (Button) findViewById(R.id.uploadBtn);
        _transferBtn = (Button) findViewById(R.id.transferBtn);
        _receiveBtn = (Button) findViewById(R.id.receiveBtn);
        _chargeSendAllBtn = (ImageButton) findViewById(R.id.chargeSendAllBtn);
        _uploadSendAllBtn = (ImageButton) findViewById(R.id.uploadSendAllBtn);
        _transferSendAllBtn = (ImageButton) findViewById(R.id.transferSendAllBtn);
        _receiveSendAllBtn = (ImageButton) findViewById(R.id.receiveSendAllBtn);
        _chargeDeleteAllBtn = (ImageButton) findViewById(R.id.chargeDeleteAllBtn);
        _uploadDeleteAllBtn = (ImageButton) findViewById(R.id.uploadDeleteAllBtn);
        _transferDeleteAllBtn = (ImageButton) findViewById(R.id.transferDeleteAllBtn);
        _receiveDeleteAllBtn = (ImageButton) findViewById(R.id.receiveDeleteAllBtn);

        //check for and delete old attachments
        checkOldAttachments();

        //test
        //_chrgBtn.setBackgroundResource(R.color.DataToSendButtonColor);
        _dbh = new DatabaseHandler(getApplicationContext());
        moduleBtnColorChngr();

        //check if pic2shop is installed, if not, install it
        if (!Utils.isFreeScannerAppInstalled(this)) {
            AlertDialog.Builder aDB = new AlertDialog.Builder(this);
            aDB.setTitle("Dependency Missing");
            aDB.setMessage("A missing dependency has been detected.  Click YES to install.");
            aDB.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.launchMarketToInstallFreeScannerApp(getApplicationContext());
                }
            });
            aDB.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // If user clicks NO, dialog is closed.
                    dialog.cancel();
                }
            });
            aDB.show();
        }

        /*configBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                configScreen();
            }
        });

        settingsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsScreen();
            }
        });*/

        _chargeSendAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // send all
                _tableNum = 1;
                send();
            }
        });

        _uploadSendAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _tableNum = 2;
                send();
            }
        });

        _transferSendAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _tableNum = 3;
                send();
            }
        });

        _receiveSendAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _tableNum = 4;
                send();
            }
        });

        _chargeDeleteAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll(1);
            }
        });

        _uploadDeleteAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll(2);
            }
        });

        _transferDeleteAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll(3);
            }
        });

        _receiveDeleteAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll(4);
            }
        });

        _chrgBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chargeParts();
            }
        });

        _uploadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadParts();
            }
        });

        _transferBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                transferParts();
            }
        });

        _receiveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                receivePO();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // go back to home screen
        String toolbarItem = item.toString();
        switch (toolbarItem) {
            case "HOME":
                this.finish();
                break;
            case "Report":
                //launch report of all records waiting to be sent
                report();
                break;
            case "Help":
                //launch help docs?
                break;
            case "About":
                //launch about Jonas activity
                about();
                break;
            case "Configure":
                //launch configActivity
                configScreen();
                break;
            case "Settings":
                //launch settingsActivity
                settingsScreen();
                break;
        }
        return true;
    }

    void about() {
        Intent aboutIntent = new Intent(getApplicationContext(), ReportActivity.class);
        aboutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(aboutIntent);
        this.finish();
    }

    void report() {
        Intent reportIntent = new Intent(getApplicationContext(), ReportActivity.class);
        reportIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(reportIntent);
        this.finish();
    }

    void receivePO() {
        //start the receivePO activity
        Intent receivePOIntent = new Intent(getApplicationContext(), ReceivePO.class);
        receivePOIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(receivePOIntent);
        this.finish();
    }

    /**
     * calls the TransferActivity.
     *
     */
    void transferParts() {
        //start the transferParts activity
        Intent transferIntent = new Intent(getApplicationContext(), TransferActivity.class);
        transferIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(transferIntent);
        this.finish();
    }

    /**
     * calls the UploadActivity.
     *
     */
    void uploadParts() {
        //start the uploadParts activity
        Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
        uploadIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(uploadIntent);
        this.finish();
    }
/*

    void testWebService() {
        // start the testWebService activity
        Intent testIntent = new Intent(getApplicationContext(), WebServiceDemo.class);
        testIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(testIntent);
    }
*/

    /**
	 * calls the ChargeActivity.
	 *
     */
    void chargeParts() {
		// start the chargeParts activity
		Intent chargeIntent = new Intent(getApplicationContext(), ChargeActivity.class);
		chargeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(chargeIntent, 1);
        this.finish();
	}
	
	/**
	 * calls the ConfigActivity.
	 *
     */
    void configScreen() {
		// start the upload parts activity
		Intent configIntent = new Intent(getApplicationContext(), ConfigActivity.class);
		configIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(configIntent);
        this.finish();
	}
	
	/**
	 * calls the SettingsActivity
	 *
     */
    void settingsScreen() {
		// start the settings activity
		Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
		settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(settingsIntent);
        this.finish();
	}

    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void send() {
        AlertDialog.Builder aDB = new AlertDialog.Builder(this);
        aDB.setTitle("Send all?");
        aDB.setMessage("Are you sure you want to send all data from this module?");
        aDB.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // When user clicks OK, the db is purged and user is sent back to main activity.
                // Auto-generated method stub
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                db.populateFields();
                //new PopulateFields().execute((Object[]) null);

                setDateTime(_tableNum);

                //db.exportDb(getApplicationContext());
                db.exportDb(_filename, _tableNum);
                //
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                // decode password - see Crypter class for methods
                String _password = SettingsActivity._password;
                _password = crypter.decode(_password);

                //testing
                //Toast.makeText(getApplicationContext(), getString(R.string.toast_decode_message) + _password, LENGTH_LONG).show();

                Mail m = new Mail(SettingsActivity._actName, _password);
                String[] toArr = SettingsActivity._to.split(";");
                //String[] toArr = { "brad.bass@jonassoftware.com",	"brad.bass@hotmail.ca", "baruch.bass@gmail.com", "tripleb33@hotmail.com" };
                m.setTo(toArr);
                m.setFrom(SettingsActivity._from);
                m.setSubject(SettingsActivity._subject);
                m.setBody(SettingsActivity._body);
                m.setHost(SettingsActivity._host);
                m.setPort(SettingsActivity._port);
                try {
                    m.addAttachment(Environment.getExternalStorageDirectory().getPath() + "/Attachments", _filename);

                    if (!m.send()) {
                        makeText(HomeActivity.this, getString(R.string.toast_email_fail_message), LENGTH_LONG).show();
                    } else {
                        makeText(HomeActivity.this, getString(R.string.toast_email_success_message), LENGTH_LONG).show();
                        //Boolean sent = true;
                        db.purgeData(_tableNum);
                    }
                } catch (final Exception e) {
                    //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                    Log.e(getString(R.string.mail_log_e_title), getString(R.string.mail_log_e_message), e);

                    e.printStackTrace();
                    String stackTrace = Log.getStackTraceString(e);

                    AlertDialog.Builder aDB = new AlertDialog.Builder(getApplicationContext());
                    aDB.setTitle("Program Exception!");
                    aDB.setMessage(stackTrace);
                    aDB.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // When user clicks OK, the db is purged and user is sent back to main activity.
                        }
                    });
                    aDB.show();
                }
                db.close();
                //change home screen module button back to original color
                _moduleBtnColorChngr(_tableNum);
            }
        });
        aDB.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user clicks NO, dialog is closed.
                dialog.cancel();
            }
        });
        aDB.show();
        //if ((save == null) || !save) {
            //saveMsg();
        //} else {

        //}
    }

    @SuppressLint("SimpleDateFormat")
    void setDateTime(Integer table) {
        // add DateTime to filename
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        Date currentLocalTime = cal.getTime();
        SimpleDateFormat date = new SimpleDateFormat(getString(R.string.filename_simple_date_format));
        date.setTimeZone(TimeZone.getDefault());
        String currentDateTime = date.format(currentLocalTime);

        setFileName(table, currentDateTime, getBaseContext());
    }

    /**
     *
     * @param currentDateTime the current date and time from setDateTime()
     * @param context   application context
     */
    void setFileName(Integer table, String currentDateTime, Context context) {
        //
        switch (table) {
            case 1:
                _filename = currentDateTime + getString(R.string.filename_extension);
                break;
            case 2:
                _filename = currentDateTime + getString(R.string.upload_filename_extension);
                break;
            case 3:
                _filename = currentDateTime + getString(R.string.whse_transfer_filename_extension);
                break;
            case 4:
                _filename = currentDateTime + getString(R.string.po_receive_filename_extension);
                break;
            default:
                break;
        }
        //_filename = currentDateTime + getString(R.string.filename_extension);

        makeText(context, getString(R.string.toast_filename_is_label)
                + _filename, LENGTH_LONG)
                .show();
    }

    void deleteAll(final Integer table) {
        final DatabaseHandler dbh = new DatabaseHandler(getApplicationContext());
        AlertDialog.Builder aDB = new AlertDialog.Builder(this);
        aDB.setTitle("Delete All Records?");
        aDB.setMessage("Are you sure you want to delete all the records from this module?");
        aDB.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // When user clicks OK, the db is purged and user is sent back to main activity.
                switch (table) {
                    case 1:
                        dbh.deleteAll("chrgData");
                        break;
                    case 2:
                        dbh.deleteAll("uploadData");
                        break;
                    case 3:
                        dbh.deleteAll("transferData");
                        break;
                    case 4:
                        dbh.deleteAll("receiveData");
                        break;
                    default:
                        break;
                }
                //dbh.deleteAll("chrgData");
                //clearVars();
                makeText(getApplicationContext(), "All records have been deleted!", LENGTH_LONG).show();
                //change home screen module button back to original color
                _moduleBtnColorChngr(table);
            }
        });
        aDB.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user clicks NO, dialog is closed.
                dialog.cancel();
            }
        });
        aDB.show();
    }

    private void checkOldAttachments() {
        // check for old attachments and if older than a certain date, delete them
        // AsyncTask?
        List<String> attachments = new ArrayList<>();
        File dir;
        try {
            dir = new File(Environment.getExternalStorageDirectory().getPath() + "/ScannerAttachments");
            File[] arrAttachments = dir.listFiles();
            for (File arrAttachment : arrAttachments) {
                attachments.add(arrAttachment.getPath());
            }
            validateOldAttachments(attachments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateOldAttachments(List<String> attachments) {
        String[] attFiles = attachments.toArray(new String[attachments.size()]);
        for (int i = 0; i < attachments.size(); i++) {
            File attFile = new File(attFiles[i]);
            //Date lastModDate = new Date(attFile.lastModified());
            Integer lastModifiedDate = Integer.valueOf(new SimpleDateFormat("ddMMyyyy", Locale.US).format(new Date(attFile.lastModified()))); //Integer.valueOf(String.valueOf(lastModDate));
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.US);
            Integer currDate = Integer.valueOf(sdf.format(cal.getTime()));
            //Time today = new Time(Time.getCurrentTimezone());
            //today.setToNow();
            //Integer currDate = today.monthDay + today.month + today.year;
            Integer days = currDate - lastModifiedDate;
            if (days > 30) {
                deleteOldAttachments(attFile);
            }
        }
    }

    private void deleteOldAttachments(File attFile) {
        // if we've found old attachments, we need to delete them
        boolean deleted = false;
        try {
            deleted = attFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (deleted) {
            Toast.makeText(getApplicationContext(),"Old attachments successfully deleted", Toast.LENGTH_LONG).show();
        }
    }

    public static void moduleBtnColorChngr() {
        //check tables for data.  If data exists, change the button color of the corresponding module and italicise the text
        for (int i = 1; i < 5; i++) {
            _moduleBtnColorChngr(i);
        }

        _dbh.checkTables();

        if (!DatabaseHandler._dataTables.isEmpty()) {
            List<String> dataTables = DatabaseHandler._dataTables;
            moduleBtnColorChngr(dataTables);
        }
    }

    // change module button color if data exists in corresponding table
    public static void moduleBtnColorChngr(List<String> dataTables) {
        for (String table : dataTables) {
            switch (table) {
                case "chrgData":
                    _chargeSendAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _chargeSendAllBtn.setImageResource(R.drawable.ic_action_email);
                    _chargeDeleteAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _chargeDeleteAllBtn.setImageResource(R.drawable.ic_action_discard);
                    _chargeSendAllBtn.setEnabled(true);
                    _chargeDeleteAllBtn.setEnabled(true);
                    break;
                case "uploadData":
                    _uploadSendAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _uploadSendAllBtn.setImageResource(R.drawable.ic_action_email);
                    _uploadDeleteAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _uploadDeleteAllBtn.setImageResource(R.drawable.ic_action_discard);
                    _uploadSendAllBtn.setEnabled(true);
                    _uploadDeleteAllBtn.setEnabled(true);
                    break;
                case "transferData":
                    _transferSendAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _transferSendAllBtn.setImageResource(R.drawable.ic_action_email);
                    _transferDeleteAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _transferDeleteAllBtn.setImageResource(R.drawable.ic_action_discard);
                    _transferSendAllBtn.setEnabled(true);
                    _transferDeleteAllBtn.setEnabled(true);
                    break;
                case "receiveData":
                    _receiveSendAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _receiveSendAllBtn.setImageResource(R.drawable.ic_action_email);
                    _receiveDeleteAllBtn.setBackgroundResource(R.drawable.roundbuttonother);
                    _receiveDeleteAllBtn.setImageResource(R.drawable.ic_action_discard);
                    _receiveSendAllBtn.setEnabled(true);
                    _receiveDeleteAllBtn.setEnabled(true);
                    break;
                default:
                    //default case?
                    break;
            }
        }
    }

    //after send, change module button color back to original
    public static void _moduleBtnColorChngr(Integer tableNum) {
        switch (tableNum) {
            case 1:
                _chargeSendAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _chargeSendAllBtn.setImageResource(R.drawable.ic_action_email_dark);
                _chargeDeleteAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _chargeDeleteAllBtn.setImageResource(R.drawable.ic_action_discard_dark);
                _chargeSendAllBtn.setEnabled(false);
                _chargeDeleteAllBtn.setEnabled(false);
                break;
            case 2:
                _uploadSendAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _uploadSendAllBtn.setImageResource(R.drawable.ic_action_email_dark);
                _uploadDeleteAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _uploadDeleteAllBtn.setImageResource(R.drawable.ic_action_discard_dark);
                _uploadSendAllBtn.setEnabled(false);
                _uploadDeleteAllBtn.setEnabled(false);
                break;
            case 3:
                _transferSendAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _transferSendAllBtn.setImageResource(R.drawable.ic_action_email_dark);
                _transferDeleteAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _transferDeleteAllBtn.setImageResource(R.drawable.ic_action_discard_dark);
                _transferSendAllBtn.setEnabled(false);
                _transferDeleteAllBtn.setEnabled(false);
                break;
            case 4:
                _receiveSendAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _receiveSendAllBtn.setImageResource(R.drawable.ic_action_email_dark);
                _receiveDeleteAllBtn.setBackgroundResource(R.drawable.roundbutton);
                _receiveDeleteAllBtn.setImageResource(R.drawable.ic_action_discard_dark);
                _receiveSendAllBtn.setEnabled(false);
                _receiveDeleteAllBtn.setEnabled(false);
                break;
            default:
                // default case?
                break;
        }
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				this.finish();
			}
		}
	}
	
	/**
	 * When user clicks the UI back button, we create a Toast.
	 */
	@Override
	public void onBackPressed() {
		makeText(getBaseContext(), getString(R.string.toast_goodbye_message), LENGTH_LONG); //.show();
		
		this.finish();
	}

    private class PopulateFields extends AsyncTask<Object, Object, Cursor> {
        //DatabaseHandler dbh = new DatabaseHandler(getApplicationContext());

        @Override
        protected Cursor doInBackground(Object... params) {
            _dbh.populateFields();
            return null;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            //
        }
    }

    private class ExportDB extends AsyncTask<Object, Object, Cursor> {

        @Override
        protected Cursor doInBackground(Object... params) {
            _dbh.exportDb(_filename, _tableNum);
            return null;
        }
    }
}