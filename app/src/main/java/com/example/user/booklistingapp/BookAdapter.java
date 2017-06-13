package com.example.user.booklistingapp;

/**
 * Created by Alexander Rashkov on 6/12/2017.
 */


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An {@link BookAdapter} knows how to create a list item layout for each book
 * in the data source (a list of {@link Book} objects).
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    /**
     * Constructs a new {@link BookAdapter}.
     *
     * @param context of the app
     * @param books   is the list of books, which is the data source of the adapter
     */
    public BookAdapter(Activity context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
        }

        // Find the book at the given position in the list of books
        Book currentBook = getItem(position);

        //Find the TextView with ID title from book_list_item.xml
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        //Get the Title from the currentBook object and set this text on the TextView.
        titleTextView.setText(currentBook.getTitle());

        //Find the TextView with ID author from book_list_item.xml
        TextView authorTextView = (TextView) listItemView.findViewById(R.id.author);
        //Get the Authors from the currentBook object and set this text on the TextView.
        authorTextView.setText(currentBook.getAuthor());

        //return the list view
        return listItemView;
    }
}
