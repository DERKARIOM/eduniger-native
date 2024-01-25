package com.ninotech.fabi.model.data;

import java.util.ArrayList;

public class Book {
   public Book(String id, String title, String blanket, ArrayList<String> category, String author, String description, String electronic, String isAudio, String isAvailable, String isPysical, String numberLikes, String numberNoLikes , String numberView) {
      mId = id;
      mTitle = title;
      mBlanket = blanket;
      mCategory = category;
      mAuthor = author;
      mDescription = description;
      mElectronic = electronic;
      mIsAudio = isAudio;
      mIsAvailable = isAvailable;
      mIsPhysical = isPysical;
      mNumberLikes = numberLikes;
      mNumberNoLikes = numberNoLikes;
      mNumberView = numberView;
   }
   public Book(String id , String blanket , String title , ArrayList<String> category , String isPhysical, String electronic , String isAudio , String numberLikes , String numberView)
   {
      mId = id;
      mBlanket = blanket;
      mTitle = title;
      mCategory = category;
      mIsPhysical = isPhysical;
      mElectronic = electronic;
      mIsAudio = isAudio;
      mNumberLikes = numberLikes;
      mNumberView = numberView;
   }
   public String getNumberView() {
      return mNumberView;
   }

   public void setNumberView(String numberView) {
      mNumberView = numberView;
   }

   public String getId() {
      return mId;
   }

   public void setId(String id) {
      mId = id;
   }

   public String getTitle() {
      return mTitle;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getBlanket() {
      return mBlanket;
   }

   public void setBlanket(String blanket) {
      mBlanket = blanket;
   }

   public ArrayList<String> getCategory() {
      return mCategory;
   }

   public void setCategory(ArrayList<String> category) {
      mCategory = category;
   }

   public String getAuthor() {
      return mAuthor;
   }

   public void setAuthor(String author) {
      mAuthor = author;
   }

   public String getDescription() {
      return mDescription;
   }

   public void setDescription(String description) {
      mDescription = description;
   }

   public String getElectronic() {
      return mElectronic;
   }

   public void setElectronic(String electronic) {
      mElectronic = electronic;
   }

   public String getIsAudio() {
      return mIsAudio;
   }

   public void setIsAudio(String isAudio) {
      mIsAudio = isAudio;
   }

   public String getIsAvailable() {
      return mIsAvailable;
   }

   public void setIsAvailable(String isAvailable) {
      mIsAvailable = isAvailable;
   }

   public String getIsPhysical() {
      return mIsPhysical;
   }

   public void setIsPhysical(String isPhysical) {
      mIsPhysical = isPhysical;
   }

   public String getNumberLikes() {
      return mNumberLikes;
   }

   public void setNumberLikes(String numberLikes) {
      mNumberLikes = numberLikes;
   }

   private String mId;
   private String mTitle;
   private String mBlanket;
   private ArrayList<String> mCategory;
   private String mAuthor;
   private String mDescription;
   private String mElectronic;
   private String mIsAudio;
   private  String mIsAvailable;
   private String mIsPhysical;
   private String mNumberLikes;
   private String mNumberNoLikes;

   private String mNumberView;
}
