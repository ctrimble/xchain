/**
 *    Copyright 2011 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.xchain.namespaces.hibernate.test;

import java.util.List;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.namespaces.hibernate.test.om.Alphabet;
import org.xchain.namespaces.hibernate.test.om.Person;
import org.xchain.namespaces.hibernate.test.om.User;
import org.xchain.namespaces.hibernate.test.om.UserNote;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 */
public abstract class BaseDatabaseTest {
	protected static List<Person> personList = new ArrayList<Person>();
	protected static List<Alphabet> alphabetList = new ArrayList<Alphabet>();
	protected static List<User> userList = new ArrayList<User>();
	protected static List<UserNote> userNotes = new ArrayList<UserNote>();
	
	private static Person createPerson(String name) {
		Person person = new Person();
		person.setName(name);
		return person;
	}
	
	protected static void populatePersonData() {
		
		// Empty out the person list.
		personList.clear();
		
		// Populate the person list.
		personList.add(createPerson("Bob"));
		personList.add(createPerson("Joe"));
		personList.add(createPerson("John"));
		personList.add(createPerson("Mary"));
		personList.add(createPerson("Sarah"));
		personList.add(createPerson("Shelly"));		
		
		// Store the person list to the database.
		persistList(personList);
	}	
	
	protected static void populateAlphabetData() {
		
		// Empty out the alphabet list.
		alphabetList.clear();
		
		// Populate the alphabet list.
		alphabetList.add(new Alphabet('a', "Alphabet a"));		
		alphabetList.add(new Alphabet('b', "Alphabet b"));		
		alphabetList.add(new Alphabet('c', "Alphabet c"));		
		alphabetList.add(new Alphabet('A', "Alphabet A"));		
		alphabetList.add(new Alphabet('B', "Alphabet B"));		
		alphabetList.add(new Alphabet('C', "Alphabet C"));		
		
		// Store the alphabet list to the database.
		persistList(alphabetList);
	}
	
	protected static void populateUserData() {
		// Empty out the user list.
		userList.clear();
		
		// Populate the user list.
		userList.add(new User("superman", "Clark"));			
		userList.add(new User("batman", "Bruce"));			
		
		// Store the user list to the database.
		persistList(userList);
	}
	
	protected static void populateUserNoteData() {
	  userNotes.clear();
	  if( userList.size() == 0 ) {
	    populateUserData();
	  }
	  User firstUser = userList.get(0);
	  for(int i = 0; i < userList.size(); i++) {
	    UserNote note = new UserNote();
	    note.setText(String.format("Note %d", i));
	    firstUser.getUserNoteSet().add(note);
	    note.setUser(firstUser);
	    userNotes.add(note);
	  }
	  persistList(userNotes);
	}
	
	protected static void persistList(List<?> data) {
		// Store the person list to the database.
		Session session = HibernateLifecycle.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();

		for (Object dataObject : data) {
			session.persist(dataObject);
		}

		transaction.commit();
		session.close();			
	}
}
