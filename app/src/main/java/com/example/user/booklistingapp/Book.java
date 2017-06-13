package com.example.user.booklistingapp;

/**
 * Created by Alexander Rashkov on 6/12/2017.
 */

public class Book {

    /**
     * Book's Title
     */
    private String mTitle;

    /**
     * Book's Author
     */
    private String mAuthor;

    /**
     * Constructs a new {@link Book} object.
     *
     * @param title  is the title of the book
     * @param author is the author of the book
     */

    public Book(String title, String author) {
        mTitle = title;
        mAuthor = author;
    }

    //Get the title
    public String getTitle() {
        return mTitle;
    }

    //Get the authors
    public String getAuthor() {
        return mAuthor;
    }

}
