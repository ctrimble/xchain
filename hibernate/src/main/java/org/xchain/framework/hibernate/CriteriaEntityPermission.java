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
package org.xchain.framework.hibernate;

import org.xchain.framework.security.IdentityManager;
import org.xchain.framework.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;

/**
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class CriteriaEntityPermission
  extends QualifiedEntityPermission
{
  private static final Logger log = LoggerFactory.getLogger(CriteriaEntityPermission.class);
  protected Criteria criteria = null;
  protected Criteria principalCriteria = null;
  protected Criteria idCriteria = null;
  protected String principalProperty = null;
  protected String idProperty = null;

  public CriteriaEntityPermission(EntityOperation operation, Class<?> entityClass, Criteria rootCriteria, Criteria principalCriteria, Criteria idCriteria, String principalProperty, String idProperty)
  {
    super(operation, entityClass);
    this.criteria = rootCriteria;
    this.principalCriteria = principalCriteria;
    this.idCriteria = idCriteria;
    this.principalProperty = principalProperty;
    this.idProperty = idProperty;
  }

  public Criteria getCriteria()
  {
    return this.criteria;
  }

  private void populateParameters( String principal, Serializable id )
  {
    if( principalProperty != null && principalCriteria != null ) {
      principalCriteria.add(Restrictions.eq(principalProperty, principal));
    }
    if( idProperty != null && idCriteria != null ) {
      idCriteria.add(Restrictions.eq(idProperty, id));
    }
  }

  @Override
  public Object qualifyPermission(Permission p) {
    Object result = null;
    final EntityPermission<?> ep = (EntityPermission<?>)p;
    try {
      populateParameters(IdentityManager.instance().getPrincipalName(), ep.getId());
      result = criteria.uniqueResult();
    } catch (Exception e) {
      log.error("Unhandled Exception", e);
    }
    return result;
  }
}
