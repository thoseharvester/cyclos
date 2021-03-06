/*
 *
 *    This file is part of Cyclos.
 *
 *    Cyclos is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    Cyclos is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Cyclos; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 */

package nl.strohalm.cyclos.controls.tokens;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.tokens.Status;
import nl.strohalm.cyclos.entities.tokens.Token;
import nl.strohalm.cyclos.utils.ActionHelper;
import org.apache.struts.action.ActionForward;

import java.util.HashMap;
import java.util.Map;

public class RefundTokenAction extends BaseTokenAction {

    @Override
    ActionForward tokenSubmit(BaseTokenForm tokenForm, Member loggedMember, ActionContext context) {

        Token token = tokenService.loadTokenByTransactionId(tokenForm.getTransactionId());
        if (token.getStatus() != Status.EXPIRED) {
            //FIXME
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("token(transactionId)", tokenForm.getTransactionId());
        params.put("token(pin)", tokenForm.getPin());
        return ActionHelper.redirectWithParams(context.getRequest(), context.getSuccessForward(), params);
    }
}
