package org.iatoki.judgels.jophiel.controllers.security;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class HasRole extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        return context.session().get("role");
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.login());
    }
}
