package com.example.user.booklistingapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Book>> {

    //Tag for the log messages
    private static final String LOG_TAG = MainActivity.class.getName();

    //This is the default value of maximum results when app is started
    private int maxResults = 10;

    //String that holds the user's input query
    private String usersInput;

    // Initial Query which will be combined with the user's input
    private static final String API_INITIAL_QUERY = "https://www.googleapis.com/books/v1/volumes?q=intitle:";

    //this string holds the other part of url query required by API
    private String queryForMaxResults = "&maxResults=";

    //Final Query will hold the full concatenated query
    private String finalQuery;

    //TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    //Progress bar to inform user that information is loading
    private ProgressBar mProgressSpinner;

    //Adapter for the list of books
    private BookAdapter mAdapter;

    //Loader manager object will be referenced twice in this activity so I use private variable
    private LoaderManager mLoaderManager;

    //Boolean that returns true or false for connection state
    private boolean mIsConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //recovering the instance state (to display proper maximum results value on the TextView)
        if (savedInstanceState != null) {
            maxResults = savedInstanceState.getInt("maxResultsToDisplay");
        }

        //set th default value of "maximum results" on start of application
        //or display the value loaded from savedInstanceState
        displayMaxResultsValue(maxResults);

        //By default there should be connection
        //but if the user clicks on search button, then a method is triggered to
        //check if there is actually an internet connection. I prefer this approach in case the user
        //have used the app before and want to see the previous results no matter if there is
        //connection or not
        mIsConnected = true;

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        //Find the empty_list_view that overlaps the listItems
        //When there is no listItems to display show this empty_list_view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);
        bookListView.setEmptyView(mEmptyStateTextView);

        //Find progress spinner
        mProgressSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create a new adapter that takes an empty list of Books as input
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(mAdapter);

        //Find decrease (minus) button to attach event listener
        Button decreaseButton = (Button) findViewById(R.id.decreaseButton);

        /**
         * Attach an event listener, when the decrease button is clicked
         * decrease by 5 the value of maximum results
         * if value is 5 then return, in order to prevent user
         * from entering less then 5 maximum results
         */
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxResults == 5) {
                    return;
                }
                maxResults -= 5;
                displayMaxResultsValue(maxResults);
            }
        });

        //Find increase (plus) button to attach event listener
        Button increaseButton = (Button) findViewById(R.id.increaseButton);

        /**
         * Attach an event listener, when the increase button is clicked
         * increase by 5 the value of maximum results
         * here the limit is 40 results, because API returns error for maxResults more than 40
         */
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxResults == 40) {
                    return;
                }
                maxResults += 5;
                displayMaxResultsValue(maxResults);
            }
        });

        //Find the search button to attach event listener
        ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        mLoaderManager = getLoaderManager();

        // Set onClick Listener on search button to display the result from the query
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Find the editText view by ID to take users input
                EditText userQueryInput = (EditText) findViewById(R.id.user_query);

                //Convert the text entered by user to string
                usersInput = userQueryInput.getText().toString();

                //If there is no text inserted by the user, just return a toast Message to
                //encourage user to insert some text
                if (usersInput != null && usersInput.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.empty_input_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Clear out the existing data ( if any)
                mAdapter.clear();
                //Clear the default message
                mEmptyStateTextView.setText("");

                //If there is inserted text then,
                //show progress bar to inform the user that data is processed
                mProgressSpinner.setVisibility(View.VISIBLE);

                //create a valid Query that will be send to the server
                finalQuery = concatenateQuery(usersInput);

                //create connectivity manager object to check for internet connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);

                //Check for internet connection
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                mIsConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                //If there is no internet connection
                //send null value to loader, so it will not populate the data from previous loader
                //on screen rotation
                if (!mIsConnected) {
                    finalQuery = null;
                }
                //restart the loader because the user have changed the input query
                //or due to no connection there is null user input
                //now onCreateLoader will return BookLoader, which takes as input
                //the new input query( "@param finalQuery" )
                mLoaderManager.restartLoader(1, null, MainActivity.this);
            }
        });

        // Initialize the loader.The ID of loader is integer of 1.
        // Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        Log.i(LOG_TAG, "initLoader is called (Loader is initiated)!");
        mLoaderManager.initLoader(1, null, this);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        return new BookLoader(this, finalQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        //Hide progressBar, because processing is finished
        mProgressSpinner.setVisibility(View.GONE);

        // Clear the adapter of previous book data
        mAdapter.clear();

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            //Update adapter
            mAdapter.addAll(books);
        } else {
            //This is the default message shown to the user when the app is started
            mEmptyStateTextView.setText(R.string.default_message);
            //Overwrite the default message if there is no internet connection
            if (!mIsConnected) {
                //display no connection message
                mEmptyStateTextView.setText(R.string.no_connection);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    //This method displays the maximum results value on the TextView
    private void displayMaxResultsValue(int maxResults) {
        TextView maxResultsTextView = (TextView) findViewById(R.id.maxResultsValue);
        maxResultsTextView.setText("" + maxResults);
    }

    //This method clears the spaces between user's input
    //by replacing the spaces with "+"
    //This makes the query valid for the API
    private String concatenateQuery(String usersTextInput) {
        //Split properly user's input because spaces are not allowed in the Query
        String[] wordsInput = usersTextInput.split("\\s+");
        String wordsToInputQuery = null;
        for (int i = 0; i < wordsInput.length; i++) {
            if (i == 0) {
                wordsToInputQuery = wordsInput[i];
            } else {
                //Concatinate the word in a proper format
                wordsToInputQuery = wordsToInputQuery + "+" + wordsInput[i];
            }
        }
        //Now attach all components of the query to make it compatible with API requirements
        usersInput = API_INITIAL_QUERY + wordsToInputQuery + queryForMaxResults + maxResults;
        return usersInput;
    }

    //Saved the state of maximum results
    //so when the screen is rotated the value will remain the same,
    //and not set to the default of 10
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("maxResultsToDisplay", maxResults);
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}
