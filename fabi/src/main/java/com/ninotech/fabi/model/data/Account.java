package com.ninotech.fabi.model.data;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.StudentTable;
import com.ninotech.fabi.model.table.UserTable;

public class Account {
    public Account(String idNumber , String name , String firstName , String email, String password, byte[] profile , long profession) {
        mIdNumber = idNumber;
        mName = name;
        mFirstName = firstName;
        mEmail = email;
        mPassword = password;
        mProfile = profile;
        mProfession = profession;
    }
    public Account()
    {
        mIdNumber=null;
        mPassword=null;
        mEmail=null;
        mProfile=null;
    }
    public Account(String idNumber , String password)
    {
        mIdNumber = idNumber;
        mPassword = password;
        mEmail = null;
        mProfile = null;
    }
    public  Account(String idNumber, String email, String password)
    {
        mIdNumber = idNumber;
        mEmail = email;
        mPassword = password;
    }
    public String inputControl(String confirPassword)
    {
        if(mIdNumber.equals("") && mEmail.equals("") && mPassword.equals("") && confirPassword.equals(""))
            return "0000"; // "Veuillez remplir ces champs svp"
        else
        {
            if(mIdNumber.equals(""))
                return "0111"; // Votre matricule svp
            if(mEmail.equals(""))
                return "1011"; // Votre email svp
            if(mPassword.equals(""))
                return "1101";
            if(confirPassword.equals(""))
                return "1110";
            if(mPassword.equals(confirPassword))
                return "1111"; // Connexion...
            else
                return "1100"; // Erreur de confirmation
        }
    }
    public String inputControl()
    {
        if(mIdNumber.equals("") && mPassword.equals(""))
            return "00"; // Votre matricule et mot de passe svp
        if(mIdNumber.equals(""))
            return "01"; // Votre matricule svp
        if(mPassword.equals(""))
            return "10"; // Votre mot de passe svp
        return "11"; // Connection
    }
    public String dataControl(String jsonData)
    {
        if(jsonData != null) {
            switch (jsonData) {
                case "existingAccount":
                    return "0111_1"; // compte existant
                case "accountNotExist":
                    return "00"; // Le compte n'existe pas
                case "incorrectPassword":
                    return "10"; // mot de passe incorrect
                case "existingEmail":
                    return "1011"; // email existant
                case "notFoundIdNumer":
                    return "0111_0"; // matricule introuvable
                case "expiresVersion":
                    return "update"; // version expires
                case "noFoundIdNumberOrEmail":
                    return "0011"; // matricule ou email introuvable
                case "false":
                    return "noConnection"; // pas de connexion internet
                default:
                    return "1111"; // ok
            }
        }
        return "noConnection";
    }
    public boolean register(Context context, String isAdmin)
    {
        UserTable userTable = new UserTable(context);
            if(userTable.isUserExist(mIdNumber))
                return true;
            else
                return (userTable.insert(mIdNumber,mName,mFirstName,mEmail,mPassword,mProfile, String.valueOf(mProfession),isAdmin));
    }
    public boolean login(Context context)
    {
        SQLiteDatabase database = context.openOrCreateDatabase(context.getResources().getString(R.string.database_name),MODE_PRIVATE,null);
        Session session = new Session(context);
        try {
            session.onCreate(database);
            session.insert(mIdNumber,mPassword);
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }
    public boolean logout(Context context)
    {
        SQLiteDatabase database = context.openOrCreateDatabase(context.getResources().getString(R.string.database_name),MODE_PRIVATE,null);
        Session session = new Session(context);
        try {
            session.onUpgrade(database,0,1);
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }

    public boolean isSession(Context context)
    {
        Session session = new Session(context);
        try {
            Toast.makeText(context, session.getIdNumber(), Toast.LENGTH_SHORT);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public String getIdNumber() {
        return mIdNumber;
    }

    public void setIdNumber(String idNumber) {
        mIdNumber = idNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public byte[] getProfile() {
        return mProfile;
    }

    public void setProfile(byte[] profile) {
        mProfile = profile;
    }
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public long getProfession() {
        return mProfession;
    }

    public void setProfession(long profession) {
        mProfession = profession;
    }
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }
    private String mIdNumber;
    private String mName;
    private String mFirstName;
    private String mPhoneNumber;
    private String mEmail;
    private long mProfession;
    private String mPassword;
    private byte[] mProfile;
}
