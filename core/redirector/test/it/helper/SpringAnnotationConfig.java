package it.helper;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.core.spring.*;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(
        {
                CommonBeans.class, BackupBeans.class,
                IntegrationTestBeans.class, IntegrationTestBackupBeans.class, IntegrationTestApplicationsBeans.class,
                IntegrationTestConfigBeans.class
        }
)
public class SpringAnnotationConfig {

    @Autowired
    private CommonBeans commonBeans;

    @Bean
    IDAOFactory daoFactory() {
        return new DAOFactory(commonBeans.connector(), false, commonBeans.xmlSerializer());
    }

    @Bean
    ISimpleServiceDAO<Whitelisted> whitelistedDAO() {
        return daoFactory().getSimpleServiceDAO(Whitelisted.class, EntityType.WHITELIST, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    IListServiceDAO<IfExpression> flavorRulesDAO() {
        return daoFactory().getListServiceDAO(IfExpression.class, EntityType.RULE, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    IListServiceDAO<IfExpression> urlRulesDAO() {
        return daoFactory().getListServiceDAO(IfExpression.class, EntityType.URL_RULE, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    IListServiceDAO<Server> serverDAO() {
        return daoFactory().getListServiceDAO(Server.class, EntityType.SERVER, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    IListServiceDAO<UrlRule> urlParamsDAO() {
        return daoFactory().getListServiceDAO(UrlRule.class, EntityType.URL_PARAMS, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    ISimpleServiceDAO<Distribution> distributionDAO() {
        return daoFactory().getSimpleServiceDAO(Distribution.class, EntityType.DISTRIBUTION, BaseDAO.NOT_COMPRESSED);
    }

    @Bean
    IListDAO<NamespacedList> namespacedListDAO() {
        return daoFactory().getNamespacedListsDAO(EntityType.NAMESPACED_LIST, BaseDAO.COMPRESSED);
    }
    
    @Bean
    IStacksDAO stacksDAO() {
        return daoFactory().createStacksDAO();
    }
    
    @Bean
    INodeVersionDAO nodeVersionDAO() {
        return daoFactory().getNodeVersionDAO();
    }
}
