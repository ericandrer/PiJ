package impl;

import spec.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Eric on 08/03/2017.
 */
public class ContactManagerImpl implements ContactManager, Serializable{

    private Set<Contact> contacts = new HashSet<>();
    private List<FutureMeeting> futureMeeting = new ArrayList<>();
    private List<PastMeeting> pastMeetingList = new ArrayList<>();
    private MeetingImpl meetingImpl;
    private static Calendar nowDate = Calendar.getInstance();
    private static int idCounter = 0;
    private int meetingId;
    private Calendar pastDate= Calendar.getInstance();
    private Calendar futureDate = Calendar.getInstance();


    @Override
    public int addFutureMeeting(Set<Contact> contacts, Calendar date) throws IllegalArgumentException,
    NullPointerException {

        for (Iterator<Contact> it = contacts.iterator();it.hasNext();) {
            if (!contacts.contains(it)) {
                throw new IllegalArgumentException("Contact not found/exist.");
            }
        }

        if (!contacts.isEmpty() || date != null) {
            if (date.after(nowDate)) {
                meetingImpl = new FutureMeetingImpl(meetingId + 1, contacts, date);
                futureMeeting.add((FutureMeeting) meetingImpl);
                meetingId++;
                return meetingImpl.getId();
            } else {
                throw new IllegalArgumentException("Date cannot be set to the past.");
            }
        } else {
            throw new NullPointerException("Contacts/date is required cannot be null.");
        }
    }

    @Override
    public PastMeeting getPastMeeting(int id) throws IllegalStateException {

        Meeting pastMeeting = getMeeting(id);
        if (pastMeeting == null) {
            return null;
        }
        return (PastMeeting) pastMeeting;
    }

    @Override
    public FutureMeeting getFutureMeeting(int id) {
        Meeting futureMeeting = getMeeting(id);
        if (futureMeeting == null) {
            return null;
        } else {
            if (futureMeeting instanceof FutureMeeting) {
                if (futureMeeting.getDate().after(nowDate)) {
                    return (FutureMeeting) futureMeeting;
                }
            } else {
                throw new IllegalArgumentException("None id for future meeting.");
            }
            return null;
        }    }

    @Override
    public Meeting getMeeting(int id) throws IllegalStateException{
        Meeting getMeeting;
        try {
            getMeeting = getPastMeeting(id);
            if (id > idCounter || id <= 0) {
                throw new IllegalArgumentException("ID out of range.");
            }
        } catch (IllegalStateException ex) {
            getMeeting = getFutureMeeting(id);
        }
        return getMeeting;
    }

    @Override
    public List<Meeting> getFutureMeetingList(Contact contact) throws IllegalArgumentException, NullPointerException {
// TODO: 19/03/17
        List<Meeting> futureMeetingList = this.futureMeeting.stream()
                .filter(m -> m.getDate().after(nowDate))
                .filter(n -> n.getContacts().contains(contact))
                .sorted(Comparator.comparing(Meeting::getDate))
                .collect(Collectors.toList());

        if (futureMeetingList.isEmpty()) {
            throw new IllegalArgumentException("Contact not found for future meetings.");

        }

        return futureMeetingList;
    }

    @Override
    public List<Meeting> getMeetingListOn(Calendar date) throws NullPointerException {
        pastDate.add(Calendar.YEAR, -1);
        if (date.equals(pastDate)) {
            return null;
        }
        if (date == null){
            throw new NullPointerException("Date cannot be null.");
        }
        return null;
    }

    @Override
    public List<PastMeeting> getPastMeetingListFor(Contact contact) {
        return null;
    }

    @Override
    public int addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) throws IllegalArgumentException, NullPointerException{
        ContactImpl contactImpl;
        if(date == null || text == null || contacts == null){
            throw new NullPointerException("Contacts/Date/Notes are null.");
        }
        PastMeeting newPastMeeting = new PastMeetingImpl(date, contacts);
        pastMeetingList.add(newPastMeeting);
        newPastMeeting = addMeetingNotes(newPastMeeting.getId(), text);
        return newPastMeeting.getId();
    }

    @Override
    public PastMeeting addMeetingNotes(int id, String text) throws IllegalArgumentException, IllegalStateException, NullPointerException{
        futureDate.add(Calendar.YEAR, 1);
        Meeting getMeeting;
        ContactImpl contactImpl;
        if (nowDate.equals(futureDate)){
            throw new IllegalStateException("Meeting is set for a date in the future.");
        }else if(nowDate== null || text == null || contacts == null){
            throw new NullPointerException("Contacts/Date/Notes are null.");
        }
        // TODO check wheter the meeting exists
        /*else if(contactImpl.getId().equals()){
            throw new IllegalArgumentException("Meeting doesn't exist.");
        }*/
        return null;
    }

    @Override
    public int addNewContact(String name, String notes) {
        Objects.requireNonNull(name, "Name is required cannot be null.");
        Objects.requireNonNull(notes, "Notes is required cannot be null.");

        if (name.equals("") || notes.equals("")) {
            throw new IllegalArgumentException("Passed an empty String parameter.");
        }
        Contact newContact = new ContactImpl(name,notes);
        contacts.add(newContact);
        return newContact.getId();

    }

    public Set<Contact> getContacts(String name) throws NullPointerException{
        Objects.requireNonNull(name,"Name is required cannot be null.");

        if(name.equals("")){
            return contacts;
        } else{
            return contacts.parallelStream()
            .filter(i -> i.getName().contains(name))
            .sorted(Comparator.comparing(Contact::getId))
            .collect(Collectors.toSet());
        }
    }

    @Override
    public Set<Contact> getContacts(int... ids) throws IllegalArgumentException{
        if (ids.length == 0) {
            throw new IllegalArgumentException("ids not provided.");
        }
        Set<Contact> resultSet = contacts.stream()
        .filter(p -> (Arrays.stream(ids).anyMatch(i -> i == p.getId())) )
        .sorted(Comparator.comparing(Contact::getId))
        .collect(Collectors.toSet());
        if (resultSet.size() != ids.length ) {
            throw new IllegalArgumentException("IDs does not correspond to a real contact");
        } else {
            return resultSet;
        }
    }


    @Override
    public void flush() {
        File file = new File("contacts.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }