package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.Preferences;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.happy.game.adventuredog.MainGDX;

public class User {
    SimpleDateFormat format;
    private int success, active, id, user, age, sex, lives, helps, tickets, version, flvl, olvl, dogType, perm;
    private String name, mail, pass, regtime, lastdate, type, message, liveDate, helpDate, ticketDate;

    public void set(User user) {
        this.success = user.success;
        this.active = user.active;
        this.id = user.id;
        this.age = user.age;
        this.sex = user.sex;
        this.lives = user.lives;
        this.helps = user.helps;
        this.tickets = user.tickets;
        this.version = user.version;
        this.flvl = user.flvl;
        this.olvl = user.olvl;
        this.dogType = user.dogType;
        this.name = user.name;
        this.mail = user.mail;
        this.pass = user.pass;
        this.regtime = user.regtime;
        this.lastdate = user.lastdate;
        this.liveDate = user.liveDate;
        this.ticketDate = user.ticketDate;
        this.helpDate = user.helpDate;
        this.type = user.type;
        this.user = user.user;
        this.message = user.message;
    }

    public void reset() {
        this.success = this.active = this.id = this.age = this.sex = this.lives = this.helps = this.tickets = this.version = this.flvl = this.olvl = this.user = this.dogType = 0;
        this.name = this.mail = this.pass = this.regtime = this.lastdate = this.liveDate = this.ticketDate = this.helpDate = this.type  = this.message = "";
    }

    public void setInWorld(Preferences prefs) {
        if (success == 1) {
            MainGDX.uID = user;
            prefs.putInteger("uid", user);
            prefs.putInteger("finished_level", flvl);
            prefs.putInteger("opened_level", olvl);
            prefs.putInteger("selected_dog", dogType);
            prefs.putInteger("lives", lives);
            prefs.putInteger("tickets", tickets);
            prefs.putInteger("helps", helps);
            prefs.putString("name", name);
            prefs.putString("mail", mail);
            prefs.putString("pass", pass);
            prefs.putInteger("perm", perm);
            prefs.flush();
        }
    }

    public void useHelp(int c) {
        helps -= c;
    }

    public void useTicket(int c) {
        tickets -= c;
    }

    public void useLives(int c) {
        lives -= c;
    }

    public int getDogType() {
        return dogType;
    }

    public int getFlvl() {
        return flvl;
    }

    public int getOlvl() {
        return olvl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getActive() {
        return active;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getHelps() {
        return helps;
    }

    public void setHelps(int helps) {
        this.helps = helps;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getVersion() {
        return version;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public Date getRegtime() {
        return getDate(regtime);
    }

    public Date getLastdate() {
        return getDate(lastdate);
    }

    public Date getLivedate() {
        return getDate(liveDate);
    }

    public Date getHelpdate() {
        return getDate(helpDate);
    }

    public Date getTicketdate() {
        return getDate(ticketDate);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail == null ? "" : mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPass() {
        return pass == null ? "" : pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    private Date getDate(String date) {
        if (date == null || date.length() < 10) return new Date();
        if (date.length() > 10) date = date.substring(0, 10);
        return new Date(Integer.parseInt(date.substring(0, 4)) - 1900, Integer.parseInt(date.substring(5, 7)) - 1, Integer.parseInt(date.substring(8)));
    }
}
