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
import org.hibernate.Query;
import java.io.Serializable;

/**
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class HqlEntityPermission
  extends QualifiedEntityPermission
{
  private static final Logger log = LoggerFactory.getLogger(HqlEntityPermission.class);
  protected String hql = null;

  public HqlEntityPermission(EntityOperation operation, Class<?> entityClass, String hql)
  {
    super(operation, entityClass);
    this.hql = hql;
  }

  public String getHql()
  {
    return this.hql;
  }

  private void populateParameters( Query query, String principal, Serializable id )
    throws IllegalArgumentException
  {
    String[] namedParameters = query.getNamedParameters();
    for( String namedParameter : namedParameters ) {
      if( "id".equals(namedParameter) ) {
        query.setParameter(namedParameter, id);
      }
      else if( "principal".equals(namedParameter) ) {
        query.setParameter(namedParameter, principal);
      }
      else {
        throw new IllegalArgumentException(String.format("Unknown parameter '%s' in permission hql '%s'.  The only defined parameters are the current entity id (:id) and the principal (:principal).", namedParameter, query.getQueryString()));
      }
    }
  }

  @Override
  public Object qualifyPermission(Permission p) {
    Object result = null;
    final EntityPermission<?> ep = (EntityPermission<?>)p;
    try {
      Query query = HibernateLifecycle.getCurrentSession().createQuery(getHql());
      populateParameters(query, IdentityManager.instance().getPrincipalName(), ep.getId());
      java.util.List<?> l = query.list();
      result = query.uniqueResult();
    } catch (Exception e) {
      log.error("Unhandled Exception", e);
    }
    return result;
  }
}
