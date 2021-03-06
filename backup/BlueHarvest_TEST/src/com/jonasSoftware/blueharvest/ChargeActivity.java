package com.jonasSoftware.blueharvest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.app.AlertDialog.Builder;
import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

/**
 * @author Brad Bass
 * @version 1.0
 * 
 * ChargeActivity allows user to scan UPC barcode, select a Name from database (see configActivity),
 * select a date, using a datePickerDialog, and allows them to enter a comment.  All fields are sent
 * to a database and then if/when user clicks on the SEND button, will extract database contents into
 * a .CSV file and then sends this file as an attachment.  
 *
 * @see android.os
 * @see android.app
 * @see android.content
 * @see android.view
 * @see android.widget
 */
//@SuppressWarnings("unused")
public class ChargeActivity extends Activity implements OnItemSelectedListener, OnDateSetListener {
	
	// Spinner element
    private Spinner spinnerWhse;
    private Spinner spinnerItem;
    private Spinner spinnerType;
    //
    static String _label;
    private static String _upc;
    private static String _date;
    private static String _filename;
    private static String _wo;
    private static String _whse;
    private static String _item;
    private static String _type;
    private static String _quantity;
    private static String _serial;

    private Boolean sent = false;
    private Boolean exit = false;
    private Boolean save = false;
	// --Commented out by Inspection (5/15/13 12:43 PM):public String label;
	private String date;
	private String comment;
    Crypter crypter = new Crypter();
    //static ArrayAdapter<String> dataAdapter;

    static EditText installField;
    static EditText jobWoField;
    static EditText quantityField;
    static EditText serialField;
    static TextView commentField;
    static TextView scanField;
    static String currentDate;

    @SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge);
        setTitle("onas Charge Parts");
		
		//buttons
		final Button scanBtn = (Button) findViewById(R.id.scanBtn);
		//final Button dateBtn = (Button) findViewById(R.id.dateBtn);
		final Button saveBtn = (Button) findViewById(R.id.saveBtn);
		final Button sendBtn = (Button) findViewById(R.id.sendBtn);
		//final Button backBtn = (Button) findViewById(R.id.btnBack);
		//final Button exitBtn = (Button) findViewById(R.id.btnExit);
		
		//*
		final TextView dateField = (TextView) findViewById(R.id.installField);
		SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.simple_date_format));
		final String currentDate = dateFormat.format(new Date());
		dateField.setText(currentDate);
		//*/
		
		installField = (EditText) findViewById(R.id.installField);
        jobWoField = (EditText) findViewById(R.id.jobWoField);
        quantityField = (EditText) findViewById(R.id.quantityField);
        serialField = (EditText) findViewById(R.id.serialField);
        commentField = (TextView) findViewById(R.id.commentField);
		scanField = (TextView) findViewById(R.id.scanField);

		//comment = commentField.getText().toString();

        // Spinner element
        spinnerWhse = (Spinner) findViewById(R.id.spinnerWhse);
        spinnerItem = (Spinner) findViewById(R.id.spinnerCostItem);
        spinnerType = (Spinner) findViewById(R.id.spinnerCostType);
 
        // Spinner click listener
        spinnerWhse.setOnItemSelectedListener(this);
        spinnerItem.setOnItemSelectedListener(this);
        spinnerType.setOnItemSelectedListener(this);
 
        // Loading spinner data from database
        loadSpinnerDataWhse();
        loadSpinnerDataItem();
        loadSpinnerDataType();

        installField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        /**
         * When user clicks the SCAN button, we start a new intent which invokes the 
         * scanner library.  We use .putExtra to send the scan mode, which we set to
         * PRODUCT_MODE, which scans UPC bar codes.
         */
        scanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Auto-generated method stub
				Intent chargeIntent = new Intent("com.google.zxing.client.android.SCAN");
					chargeIntent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(chargeIntent, 0);
				
			}
		});
		
        /**
         * When user clicks the SAVE button, we insert all fields into the database
         */
		saveBtn.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("StringBufferReplaceableByString")
            @Override
			public void onClick(View v) {
				// add fields from new table
				if(_date == null) {
					date = currentDate.replaceAll("\\s+", "").replaceAll("/", "");
                    _date = date;
				}
				if(_upc == null) {
					_upc = scanField.getText().toString();
				}
				DatabaseHandler saveToDb = new DatabaseHandler(getApplicationContext());

				comment = commentField.getText().toString();
                _quantity = quantityField.getText().toString();
                _wo = jobWoField.getText().toString();
                _serial = serialField.getText().toString();
                _whse = spinnerWhse.getSelectedItem().toString();
                _item = spinnerItem.getSelectedItem().toString();
                _type = spinnerType.getSelectedItem().toString();

                saveToDb.saveToDb(_whse, _wo, _item, _type, _upc, _quantity,
                        _serial, comment, _date, getBaseContext());

				save = true;
				_upc = null;
				scanField.setText(null);
				commentField.setText(null);
                jobWoField.setText(null);
                quantityField.setText(null);
                serialField.setText(null);
                dateField.setText(currentDate);
                spinnerWhse.setSelection(0);
                spinnerItem.setSelection(0);
                spinnerType.setSelection(0);
            }
		});
		
		/**
		 * When user clicks the SEND button we first call db.exportDB() from
         * DatabaseHandler which exports the contents of the chrgData table
         * into a .csv file.  We then create a new Object m of Mail class to
         * create and send an email with the csv file as an attachment.
		 */
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {

                send();
                //testService();

		    }
	    });
    }
	
	void saveMsg() {
		Builder aDB = new Builder(this);
		aDB.setTitle(getString(R.string.savemsg_dialog_title));
		aDB.setMessage(getString(R.string.savemsg_window_message));
		aDB.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// If user clicks NO, dialog is closed.
				dialog.cancel();				
			}
		});
		aDB.show();
	}
	
	//*    
	@SuppressLint("SimpleDateFormat")
    void setDateTime() {
    	// add DateTime to filename
    	Calendar cal = Calendar.getInstance(TimeZone.getDefault());
    	Date currentLocalTime = cal.getTime();
    	SimpleDateFormat date = new SimpleDateFormat(getString(R.string.filename_simple_date_format));
    	date.setTimeZone(TimeZone.getDefault()); 
    	String currentDateTime = date.format(currentLocalTime);
    	
    	setFileName(currentDateTime, getBaseContext());
    }
    //*/
    //*
    void setFileName(String currentDateTime, Context context) {
    	//
    	_filename = currentDateTime + getString(R.string.filename_extension);

    	makeText(context, getString(R.string.toast_filename_is_label)
                + _filename, LENGTH_LONG)
                .show();
    }    
    //*/
	
	/**
	 * When user clicks on the DATE button, we call this method to show the datePicker.
	 *
     */
    void showDatePickerDialog() {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

    private void loadSpinnerDataWhse() {
        // calls method to load spinner
        loadSpinnerData("1");
        // load the spinner
        //spinnerWhse.setAdapter(dataAdapter);
    }

    private void loadSpinnerDataItem() {
        loadSpinnerData("2");

        //spinnerItem.setAdapter(dataAdapter);
    }

    private void loadSpinnerDataType() {
        loadSpinnerData("3");

        //spinnerType.setAdapter(dataAdapter);
    }
		
	/**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(String tableName) {
        // load WHSE, COST ITEM and COST TYPE spinners from DB
        //String spinner = tableName;
        // database handler
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> labels = db.getAllLabels(tableName);
 
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);
 
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //spinnerWhse.setAdapter(dataAdapter);
        if (tableName.equals("1")) {
            spinnerWhse.setAdapter(dataAdapter);
        } else if (tableName.equals("2")) {
            spinnerItem.setAdapter(dataAdapter);
        } else if (tableName.equals("3")) {
            spinnerType.setAdapter(dataAdapter);
        }

        //return dataAdapter;
    }
    
    /**
     * When user selects a name from the spinner.
     *
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        // modify for new spinners
        // On selecting a spinner item
        String label = parent.getItemAtPosition(position).toString();

        setLabel(label);

    }
    
    /**
     * Initialise the static variable _label 
     * 
     * @param label		Name that user selected from the spinner
     */
    void setLabel(String label) {
    	_label = label;
    	
    }

    /**
     * Initialise the static variable _upc
     * 
     * @param scanResult	upc code returned from the scanner
     */
    void setUpc(String scanResult) {
    	_upc = scanResult;
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Auto-generated method stub
 
    }

    /**
     * When scanner returns a result, we verify ok then send to the text box.
     * 
     * @param requestCode   requestCode
     * @param resultCode    resultCode
     * @param intent        intent
     */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	   if (requestCode == 0) {
	      if (resultCode == RESULT_OK) {
	         String scanResult = intent.getStringExtra("SCAN_RESULT");
	         //String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	         // Handle successful scan
	         EditText code =(EditText)findViewById(R.id.scanField);
	         code.setText(scanResult);
	         setUpc(scanResult);
	      } else if (resultCode == RESULT_CANCELED) {
	         // Handle cancel
             Toast.makeText(getApplicationContext(),getString(R.string.toast_failed_to_scan_message),LENGTH_LONG).show();
	      }
	   }
	}

	/**
	 * When user selects a date from the datePicker, we first set the contents of the dateField
	 * with the date the user selected.  We then format the date string to be sent to the database.
	 * we then initialise the static variable _date to the new formatted date.
	 * 
	 * @param view      view
	 * @param year		the year returned from the datePicker
	 * @param month		the month returned from the datePicker
	 * @param day		the day returned from the datePicker
	 */
	//@SuppressLint("DefaultLocale")
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Auto-generated method stub
		Log.w("DatePicker",year + " / " + (month+1) + " / " + day);
		((EditText) findViewById(R.id.installField)).setText(year + " / " + (month+1) + " / " + day);
		date = year + "" + (month+1) + "" + day;
		String _month = String.format("%02d", month+1);
		String _day = String.format("%02d", day);
		_date = year + "" + _month + "" + _day;
    }

    public void testService() {
        WebServiceDemo wsd = new WebServiceDemo();

        setDateTime();

        if(_date == null) {
            date = currentDate.replaceAll("\\s+", "").replaceAll("/", "");
            _date = date;
        }
        if(_upc == null) {
            _upc = scanField.getText().toString();
        }
        //DatabaseHandler saveToDb = new DatabaseHandler(getApplicationContext());

        comment = commentField.getText().toString();
        _quantity = quantityField.getText().toString();
        _wo = jobWoField.getText().toString();
        _serial = serialField.getText().toString();
        _whse = spinnerWhse.getSelectedItem().toString();
        _item = spinnerItem.getSelectedItem().toString();
        _type = spinnerType.getSelectedItem().toString();

        wsd.testService(_whse,_wo,_item,_type,_upc,_quantity,_serial,comment,_date);
    }

    void send() {
        if ((save == null) || !save) {
            saveMsg();
        } else {
            // Auto-generated method stub
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            db.populateFields();

            setDateTime();

            //db.exportDb(getApplicationContext());
            db.exportDb(getApplicationContext(), _filename);
            //
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // decode password - see Crypter class for methods
            String _password = SettingsActivity._password;
            _password = crypter.decode(_password);

            //testing
            Toast.makeText(getApplicationContext(), getString(R.string.toast_decode_message) + _password, LENGTH_LONG).show();

            Mail m = new Mail(SettingsActivity._actName, _password);
            String[] toArr = SettingsActivity._to.split(";");
            //String[] toArr = { "brad.bass@jonassoftware.com",	"brad.bass@hotmail.ca", "baruch.bass@gmail.com", "tripleb33@hotmail.com" };
            m.setTo(toArr);
            m.setFrom(SettingsActivity._from);
            m.setSubject(SettingsActivity._subject);
            m.setBody(SettingsActivity._body);
            try {
                m.addAttachment(Environment.getExternalStorageDirectory().getPath(),_filename);

                if (!m.send()) {
                    makeText(ChargeActivity.this, getString(R.string.toast_email_fail_message), LENGTH_LONG).show();
                } else {
                    makeText(ChargeActivity.this, getString(R.string.toast_email_success_message), LENGTH_LONG).show();
                    sent = true;
                }
            } catch (final Exception e) {
                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                Log.e(getString(R.string.mail_log_e_title), getString(R.string.mail_log_e_message), e);

                e.printStackTrace();
                String stackTrace = Log.getStackTraceString(e);

                Builder aDB = new Builder(this);
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
        }
    }
	
	/**
	 * closes the ChargeActivity.
	 */
    void endActivity() {
		this.finish();
	}
	
	void exitApp() {
		makeText(getBaseContext(), getString(R.string.toast_goodbye_message), LENGTH_LONG).show();
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		Intent chrgAct = new Intent();
		setResult(RESULT_OK, chrgAct);
		db.purgeChrgData();
		finish();
	}
	
	void backToMain() {
		//Toast.makeText(getBaseContext(), "Thanks for using BlueHarvest!", Toast.LENGTH_LONG).show();
		//String TABLE_CHRG_DATA = "TABLE_CHRG_DATA";
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		//Intent i = new Intent(getApplicationContext(), MainActivity.class);
		db.purgeChrgData();
		db.close();
		endActivity();
	}
	
	/**
	 * When called, will pop an AlertDialog asking user if they are sure they want
	 * to exit the screen.  This is attached to UI back button, BACK button and EXIT
	 * button.  If user selects YES, we purge the database and send them back to the
	 * main activity.
	 */
	@Override
	public void onBackPressed() {
        if ((sent == null) || sent) {
            if ((sent == null) || !sent || (exit == null) || !exit) {
                backToMain();
            } else {
                exitApp();
            }
        } else {
            Builder aDB = new Builder(this);
            aDB.setTitle(getString(R.string.onbackpress_dialog_title));
            aDB.setMessage(getString(R.string.onbackpress_dialog_message));
            aDB.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // When user clicks OK, the db is purged and user is sent back to main activity.
                    if((exit != null) && exit) {
                        exitApp();
                    } else {
                        backToMain();
                    }
                }
            });
            aDB.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // If user clicks NO, dialog is closed.
                    exit = false;
                    dialog.cancel();
                }
            });
            aDB.show();
        }
    }
}

