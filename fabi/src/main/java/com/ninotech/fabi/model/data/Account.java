package com.ninotech.fabi.model.data;

import android.content.Context;
import android.view.View;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.LoginActivity;
import com.ninotech.fabi.model.table.UserTable;

public class Account {
    public Account(String idNumber, String email, String password, String profile) {
        mIdNumber = idNumber;
        mEmail = email;
        mPassword = password;
        mProfile = profile;
    }
    public String inputControlRegister(String confirPassword)
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
            {
                return "1111"; // Connexion...
            }else
            {
                return "1100"; // Erreur de confirmation
            }
        }
    }
    public String dataControlRegister(String jsonData)
    {
        if(jsonData != null) {
            if (jsonData.equals("existingAccount"))
                return "0111_1"; // compte existant
            else {
                if (jsonData.equals("existingEmail")) {
                    return "1011"; // email existant
                } else {
                    if (jsonData.equals("notFoundIdNumer")) {
                        return "0111_0"; // matricule introuvable
                    } else {
                        if (jsonData.equals("expiresVersion")) {
                            return "update"; // version expires
                        } else {
                            return "1111"; // tout est correcte
                        }
                    }
                }
            }
        }
        return null;
    }
    public boolean register(Context context,String nom , String prenom , String status , String email)
    {
        UserTable userTable = new UserTable(context);
        return (userTable.insert(mIdNumber,nom,prenom,status,email));
    }
    public String inputControlLogin(String idNumber , String password)
    {
        mIdNumber = idNumber;
        mPassword = password;
        if(mIdNumber.equals("") && mPassword.equals(""))
            return "00"; // Votre matricule et mot de passe svp
        if(!mPassword.equals("") && mPassword.equals("")){
            return "01"; // Votre matricule svp
        }
        if(!mIdNumber.equals("") && mPassword.equals(""))
        {
           return "10"; // Votre mot de passe svp
        }
        if(!mIdNumber.equals("") && !mPassword.equals(""))
            return "11"; // Connection
        return null;
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

    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }

    private String mIdNumber;
    private String mEmail;
    private String mPassword;
    private String mProfile;
}
