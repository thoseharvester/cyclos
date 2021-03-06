/*
 This file is part of Cyclos.

 Cyclos is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Cyclos is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Cyclos; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.services.elements;

import java.util.Calendar;
import java.util.List;

import nl.strohalm.cyclos.dao.members.RemarkDAO;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.members.remarks.BrokerRemark;
import nl.strohalm.cyclos.entities.members.remarks.BrokerRemarkQuery;
import nl.strohalm.cyclos.entities.members.remarks.GroupRemark;
import nl.strohalm.cyclos.entities.members.remarks.GroupRemarkQuery;
import nl.strohalm.cyclos.entities.members.remarks.Remark;
import nl.strohalm.cyclos.entities.members.remarks.RemarkQuery;
import nl.strohalm.cyclos.entities.members.remarks.Remark.Nature;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.access.PermissionRequestorImpl;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.utils.validation.Validator;

/**
 * Implementation for remark service
 * @author luis
 * @author Jefferson Magno
 */
public class RemarkServiceImpl implements RemarkService {

    private RemarkDAO         remarkDao;
    private PermissionService permissionService;

    @SuppressWarnings("unchecked")
    public List<BrokerRemark> listBrokerRemarksFor(final Element subject) {
        return (List<BrokerRemark>) list(subject, Remark.Nature.BROKER, Remark.Relationships.WRITER, BrokerRemark.Relationships.OLD_BROKER, BrokerRemark.Relationships.NEW_BROKER);
    }

    @SuppressWarnings("unchecked")
    public List<GroupRemark> listGroupRemarksFor(final Element subject) {
        final PermissionRequestorImpl permissionRequestor = new PermissionRequestorImpl();

        switch (subject.getNature()) {
            case ADMIN:
                permissionRequestor.adminPermissions("adminAdmins", "changeGroup");
                break;
            case MEMBER:
                permissionRequestor.adminPermissions("adminMembers", "changeGroup");
                permissionRequestor.manages((Member) subject);
                break;
            case OPERATOR:
                permissionRequestor.memberPermissions("memberOperators", "manage");
                permissionRequestor.manages(((Operator) subject).getMember());
                break;
            default:
                throw new PermissionDeniedException();
        }

        if (!permissionService.checkPermissions(permissionRequestor)) {
            throw new PermissionDeniedException();
        }

        return (List<GroupRemark>) list(subject, Remark.Nature.GROUP, GroupRemark.Relationships.OLD_GROUP, GroupRemark.Relationships.NEW_GROUP);
    }

    public Remark load(final Long id, final Relationship... fetch) {
        return remarkDao.load(id, fetch);
    }

    public BrokerRemark save(final BrokerRemark remark) {
        return doSave(remark);
    }

    public GroupRemark save(final GroupRemark remark) {
        return doSave(remark);
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setRemarkDao(final RemarkDAO remarkDAO) {
        remarkDao = remarkDAO;
    }

    public void validate(final Remark remark) throws ValidationException {
        if (LoggedUser.isValid()) {
            remark.setWriter(LoggedUser.element());
        }
        if (remark.getDate() == null) {
            remark.setDate(Calendar.getInstance());
        }
        getValidator(remark.getNature()).validate(remark);
    }

    private <R extends Remark> R doSave(final R remark) {
        validate(remark);
        return remarkDao.insert(remark);
    }

    private Validator getValidator(final Nature nature) {
        final Validator validator = new Validator("remark");

        validator.property("subject").required();
        validator.property("comments").required().maxLength(4000);
        validator.property("date").required();

        switch (nature) {
            case GROUP:
                validator.property("oldGroup").required();
                validator.property("newGroup").required();
                break;
            case BROKER:
                validator.property("oldBroker").required();
                validator.property("newBroker").required();
                break;
        }
        return validator;
    }

    private List<? extends Remark> list(final Element subject, final Nature type, final Relationship... fetch) {
        RemarkQuery query = null;
        switch (type) {
            case BROKER:
                query = new BrokerRemarkQuery();
                break;
            case GROUP:
                query = new GroupRemarkQuery();
                break;
        }
        query.setSubject(subject);
        query.fetch(fetch);
        return remarkDao.search(query);
    }

}