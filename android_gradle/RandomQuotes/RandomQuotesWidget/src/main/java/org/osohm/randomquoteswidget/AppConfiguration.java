/* 
 * Copyright (C) 2014-2016 Camilo Tejeiro 
 * Licensed under GPLv3, see "license" file for details. 
 */

package org.osohm.randomquoteswidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FilenameFilter;

// import our shared common library classes.
import org.osohm.randomquoteslib.PreferencesStorage;
import org.osohm.randomquoteslib.FilesProcessor;

import ar.com.daidalos.afiledialog.FileChooserActivity;

/***********************************************************************
 * App Configuration
 * This class simply displays a configure screen 
 * where the user can input the text file(s) he wants the widget to read 
 * from.
 * @author Camilo Tejeiro ,=,e for Osohm 
 **********************************************************************/
public class AppConfiguration extends Activity
{
    private static final String LOG_TAG = AppConfiguration.class.getName();
    
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int PICK_FOLDER_REQUEST_CODE = 2;
                
    // view objects.
    private static final int MINS_TO_MS_CONVERSION_FACTOR = 60*1000;
                
    // view objects.
    private EditText fileEditText;
    private EditText logEditText;
                
    // declare and reset our AppwidgetID.
    private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    // when the activity is created.
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // when "creating": Call super onCreate first (prevents nullPointers).
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG, "onCreate: Configuration Screen loaded");
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // receive the intent and get the unique key.
        Intent receivedIntent = getIntent();
        Bundle extras = receivedIntent.getExtras();
        if (extras != null)
        {
            myAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                            AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Finish if they gave us an intent without a widget ID, we can't proceed.
        if (myAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 
        {
            finish();
        }
        
        // now we will render the UI for the configure Activity.
        setContentView(R.layout.configure_layout);

        // we will have a file name input that can be configured in the widget.
        fileEditText = (EditText) findViewById(R.id.config_files_edittext);
        
        // Where we will display messages regarding configuration details.
        logEditText = (EditText) findViewById(R.id.config_log_edittext);
    }
    
    /**
     * Save User Configuration
     * Method called by Android onClick "OK" in our app configuration screen. 
     * See the onClick property in the configure_layout.xml button element.
     * @param View  
     **/
    public void saveUserConfiguration(View view) 
    {
        Log.i(LOG_TAG, "onClick: SaveUserConfiguration");
        
        final Context context = AppConfiguration.this;
        
        // To keep track of errors to return.
        boolean configStatus = false;
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        // create our preferences storage object, pass our widget specific properties.
        PreferencesStorage storedPreferences = new PreferencesStorage(context, myAppWidgetId);
        
        // create our filesProcessor Object, pass our underlying preferences storage. 
        FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
        
        configMessageLog = "Our Widget Instance ID: " + myAppWidgetId;
        Log.d(LOG_TAG, configMessageLog);
        logEditText.setText("* " +  configMessageLog + "\n");
        
        // get the user input.
        String userFilePaths = fileEditText.getText().toString();
        
        // display user file paths.
        configMessageLog = "User File Paths: " + userFilePaths;
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");

        // now lets break the string by our separator.
        String[] filePathsArray = userFilePaths.split(";"); 
        
        // check our files to make sure everything is correct.
        configStatus = filesProcessor.checkFilePaths(filePathsArray); 
        
        if (configStatus == false)
        {
            configMessageLog = "Incorrect file path or no .txt file extension";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("* " + configMessageLog + ", please re-submit" + "\n"); 
            
            // exit, let the user correct entries.                    
            return;
        }

        // user data is valid, proceed to store it.
        configMessageLog = "Storing user preferences";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");

        // clear prior user preferences. 
        storedPreferences.deleteUserPreferences();

        // let's store the valid user filePaths.
        storedPreferences.updateUserFilePaths(filePathsArray);

        configMessageLog = "Processing Text Files to Quotes";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");
        
        // and let's actually process the files.
        filesProcessor.processTextFiles();
        
        // End log info
        configMessageLog = "Configuration Complete";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("***" + configMessageLog + "*** \n");
        
        // When an App Widget uses a configuration Activity, it is 
        // the responsibility of the Activity to update the App 
        // the first time when the Widget when configuration is complete.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         
        // pass back the current processed data.
        String[] currentDataArray = storedPreferences.getCurrentDataPreferences();
        RandomQuotesWidget.updateAppWidget(context, appWidgetManager, myAppWidgetId, currentDataArray);
        
        // the configuration Activity should always return a result. 
        // The result should include the App Widget ID passed by 
        // the Intent that launched the Activity.
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
        setResult(RESULT_OK, resultValue);
        
        // end the activity.
        finish();
        
    }

    /**
     * Browse File Directories
     * Method called by Android onClick "Browse" in our app configuration screen. 
     * See the onClick property in the configure_layout.xml button element.
     * @param View  
     **/    
    public void browseFileDirectories(View view) 
    {
        Log.i(LOG_TAG, "onClick: browseFileDirectories");
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        // create intent to launch the filechooser in default file mode.
        Intent fileIntent = new Intent(AppConfiguration.this, FileChooserActivity.class);
        startActivityForResult(fileIntent, PICK_FILE_REQUEST_CODE);
    }

    /**
     * Scan File Directories
     * Method called by Android onClick "Scan" in our app configuration screen. 
     * See the onClick property in the configure_layout.xml button element.
     * @param View  
     **/    
    public void scanFileDirectories(View view) 
    {
        Log.i(LOG_TAG, "scanFileDirectories");
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        // create intent to launch the filechooser in folder mode.
        Intent folderIntent = new Intent(AppConfiguration.this, FileChooserActivity.class);
        folderIntent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
        this.startActivityForResult(folderIntent, PICK_FOLDER_REQUEST_CODE);        
    }

    // called after the browser file picker returns.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    { 
        Log.i(LOG_TAG, "onActivityResult, Request code: " + requestCode 
            + ", Result Code: " + resultCode);
        
        if (resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            
            if (requestCode == PICK_FILE_REQUEST_CODE)
            {
                File filePicked = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                
                String filePath = filePicked.getAbsolutePath();
                
                Log.d(LOG_TAG, "got filePath: " + filePath);  

                // is the editText empty, then don't prefix ';' just set.
                if (fileEditText.getText().toString().trim().length() == 0)
                    fileEditText.setText(filePath);
                else
                    fileEditText.append(";" + filePath);                    
            }
            else if (requestCode == PICK_FOLDER_REQUEST_CODE)
            {
                File folderPicked = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);

                Log.d(LOG_TAG, "got folderPath: " + folderPicked.getAbsolutePath());  
                
                // scan for text files in directory: List files and filter by ".txt"
                File[] filesScanned = folderPicked.listFiles(new FilenameFilter() 
                {
                    public boolean accept(File dir, String name) 
                    {
                        return name.toLowerCase().endsWith(".txt");
                    }
                });
                
                // add one by one to the editText View
                for (int i = 0; i < filesScanned.length; ++i) 
                {
                    String filePath = filesScanned[i].getAbsolutePath();
                    
                    Log.d(LOG_TAG, "File Scanned: " + filePath);
                    
                    // is the editText empty, then don't prefix ';'
                    if (fileEditText.getText().toString().trim().length() == 0)
                        fileEditText.setText(filePath);
                    else
                        fileEditText.append(";" + filePath);                      
                }                                
            }
            else
            {
                // Result Failed
                Log.d(LOG_TAG, "Unknown request code"); 
            }
        }
        else
        {
            // Result Failed
            Log.d(LOG_TAG, "Result Code: Operation not successful");            
        }
    }
}
