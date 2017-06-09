/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.decider.service;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.appDecider.Partner;
import com.comcast.redirector.api.model.appDecider.Partners;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class PartnersService implements IPartnersService {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    private IListDAO<Partner> partnersDAO;

    @Autowired
    private IDeciderRulesService deciderRulesService;

    @Override
    public Partners getAllPartners() {
        lock.readLock().lock();
        try {
            Partners partners = new Partners();
            for (Partner partner : partnersDAO.getAll()) {
                partners.addPartner(partner);
            }
            return partners;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Partner getPartnerById(String partnerId) {
        return partnersDAO.getById(partnerId);
    }

    @Override
    public void savePartners(Partners partners) {
        lock.writeLock().lock();
        try {
            Partners currentPartners = getAllPartners();

            List<String> partnersIds = new ArrayList<>();
            for (Partner partner : partners.getPartners()) {
                if (partner.getId() == null) {
                    continue;
                }
                partnersIds.add(partner.getId());
                partnersDAO.saveById(partner, partner.getId());
            }

            // remove deleted partners (that are absent in the input data)
            if (currentPartners.getPartners() != null) {
                for (Partner partner : currentPartners.getPartners()) {
                    if (!partnersIds.contains(partner.getId())) {
                        partnersDAO.deleteById(partner.getId());
                    }
                }
            }
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void savePartner(Partner partner) {
        lock.writeLock().lock();
        try {
            partnersDAO.saveById(partner, partner.getId());
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deletePartner(String partnerId) {
        lock.writeLock().lock();
        try {
            Collection<IfExpression> rules = new ArrayList<>();

            // get all decider rules
            rules.addAll(deciderRulesService.getRules().getItems());

            // need to ensure that the namespace we are going to remove is not used in some rule
            Partner partner = getPartnerById(partnerId);
            if (partner != null) {
                ModelValidationFacade.validatePartner(partner, rules);
            }
            partnersDAO.deleteById(partnerId);
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to remove partner item '%s' due to validation error(s). %s",  partnerId, ex.getMessage());
            throw new WebApplicationException(error, ex, Response.Status.BAD_REQUEST);
        }
        catch (RedirectorDataSourceException rde) {
            //rethrowing so it will be intercepted by mapper
            throw rde;
        }
        catch (Exception ex) {
            throw new WebApplicationException(new NotSerializableException("Can't serialize object"), Response.Status.BAD_REQUEST);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
