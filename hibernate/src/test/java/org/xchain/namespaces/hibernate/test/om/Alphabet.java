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
package org.xchain.namespaces.hibernate.test.om;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Entity;
import org.hibernate.validator.NotNull;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
@Entity
public class Alphabet
	implements Serializable
{
	@NotNull private Character id = null;
	@NotNull private String name = null;
	
	public Alphabet() {
		super();
	}
	
	public Alphabet(Character id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	@Id
	public Character getId() {
		return id;
	}
	public void setId(Character id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
