package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;

public interface AccessTokenDao extends Dao<Long, AccessTokenModel> {

    boolean existsByToken(String token);

    AccessTokenModel findByCode(String code);

    AccessTokenModel findByToken(String token);

}
