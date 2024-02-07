package com.ninotech.fabi.model.data;

import java.util.ArrayList;

public class Book {
   public Book(String id, String title, String blanket, ArrayList<String> category, String author, String description, String electronic, String isAudio, String isAvailable, String isPhysic, int numberLikes, int numberNoLikes , int numberView) {
      mId = id;
      mTitle = title;
      mBlanket = blanket;
      mCategory = category;
      mAuthor = author;
      mDescription = description;
      mElectronic = electronic;
      mIsAudio = isAudio;
      mIsAvailable = isAvailable;
      mIsPhysic = isPhysic;
      mNumberLikes = numberLikes;
      mNumberNoLikes = numberNoLikes;
      mNumberView = numberView;
   }
   public Book(String id , String blanket , String title , ArrayList<String> category , String isPhysic, String electronic , String isAudio , int numberLikes , int numberView)
   {
      mId = id;
      mBlanket = blanket;
      mTitle = title;
      mCategory = category;
      mIsPhysic = isPhysic;
      mElectronic = electronic;
      mIsAudio = isAudio;
      mNumberLikes = numberLikes;
      mNumberView = numberView;
   }
   public void like()
   {
      mNumberLikes++;
   }
   public void disLike()
   {
      mNumberLikes--;
   }
   public void noLike()
   {
      mNumberNoLikes++;
   }
   public void disNoLike()
   {
      mNumberNoLikes--;
   }
   public Book(String id)
   {
      mId = id;
      mCategory = new ArrayList<>();
   }
   public int getNumberView() {
      return mNumberView;
   }

   public void setNumberView(int numberView) {
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

   public String getIsPhysic() {
      return mIsPhysic;
   }

   public void setIsPhysic(String isPhysic) {
      mIsPhysic = isPhysic;
   }

   public int getNumberLikes() {
      return mNumberLikes;
   }

   public void setNumberLikes(int numberLikes) {
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
   private String mIsPhysic;
   private int mNumberLikes;

   public int getNumberSubscribe() {
      return mNumberSubscribe;
   }

   public void setNumberSubscribe(int numberSubscribe) {
      mNumberSubscribe = numberSubscribe;
   }

   private int mNumberSubscribe;
   public void subscribe()
   {
      mNumberSubscribe++;
   }
   public void desSubscribe()
   {
      mNumberSubscribe--;
   }

   public int getNumberNoLikes() {
      return mNumberNoLikes;
   }

   public void setNumberNoLikes(int numberNoLikes) {
      mNumberNoLikes = numberNoLikes;
   }

   private int mNumberNoLikes;

   private int mNumberView;
}
