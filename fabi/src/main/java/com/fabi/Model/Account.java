package com.fabi.Model;
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
