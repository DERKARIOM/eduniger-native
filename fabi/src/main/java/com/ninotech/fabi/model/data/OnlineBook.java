package com.ninotech.fabi.model.data;

public class OnlineBook extends Book {
   public OnlineBook(String id, String title, String cover, String category, String author, String description, String electronic, String isAudio, String isAvailable, String isPhysic, int numberLikes, int numberNoLikes , int numberView) {
      super(id,title,category,author,description);
      mCover = cover;
      mElectronic = electronic;
      mIsAudio = isAudio;
      mIsAvailable = isAvailable;
      mIsPhysic = isPhysic;
      mNumberLikes = numberLikes;
      mNumberNoLikes = numberNoLikes;
      mNumberView = numberView;
   }
   public OnlineBook(String id , String cover, String title , String category , String isPhysic, String electronic , String isAudio , int numberLikes , int numberView)
   {
      super(id,title,category);
      mCover = cover;
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
   public OnlineBook(String id)
   {
      super(id);
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

   public String getCover() {
      return mCover;
   }

   public void setCover(String cover) {
      mCover = cover;
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
   public int getNumberSubscribe() {
      return mNumberSubscribe;
   }

   public void setNumberSubscribe(int numberSubscribe) {
      mNumberSubscribe = numberSubscribe;
   }

   private String mCover;
   private String mElectronic;
   private String mIsAudio;
   private  String mIsAvailable;
   private String mIsPhysic;
   private int mNumberLikes;
   private int mNumberSubscribe;

   private int mNumberNoLikes;

   private int mNumberView;

}
