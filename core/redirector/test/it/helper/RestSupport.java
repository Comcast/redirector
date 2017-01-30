package it.helper;

import com.comcast.redirector.api.model.AppNames;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.dataaccess.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.comcast.redirector.common.RedirectorConstants.*;
import static com.comcast.redirector.common.RedirectorConstants.EndpointPath.VALID_MODEL_EXISTS;

@Controller
@Path("/redirectorWebService/data")
public class RestSupport {
    private static final Logger log = LoggerFactory.getLogger(RestSupport.class);

    @Autowired
    private ISimpleServiceDAO<Whitelisted> whitelistedDAO;
    @Autowired
    private IListServiceDAO<IfExpression> flavorRulesDAO;
    @Autowired
    private IListServiceDAO<IfExpression> urlRulesDAO;
    @Autowired
    private IListServiceDAO<Server> serverDAO;
    @Autowired
    private IListServiceDAO<UrlRule> urlParamsDAO;
    @Autowired
    private ISimpleServiceDAO<Distribution> distributionDAO;
    @Autowired
    private IListDAO<NamespacedList> namespacedListDAO;
    @Autowired
    private IStacksDAO stacksDAO;
    @Autowired
    private INodeVersionDAO nodeVersionDAO;
    @Autowired
    private CommonBeans commonBeans;

    @GET
    @Path("distributions/{appName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDistribution(@PathParam("appName") String appName) {
        if (commonBeans.connector().isConnected()) {
            return Response.ok(extractDistribution(appName)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path(SERVERS_CONTROLLER_PATH + "/{appName}/" + RedirectorConstants.DEFAULT_SERVER_NAME + "/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServer(@PathParam("appName") final String appName) {
        if (commonBeans.connector().isConnected()) {
            return Response.ok(extractServer(appName)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path(RULES_CONTROLLER_PATH + "/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllRules(@PathParam("serviceName") final String serviceName) {
        if (commonBeans.connector().isConnected()) {
            return Response.ok(extractFlavorRulesService(serviceName)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path(URL_RULES_CONTROLLER_PATH + "/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllUrlRules(@PathParam("serviceName") final String serviceName) {
        if (commonBeans.connector().isConnected()) {
            return Response.ok(extractAllUrlRules(serviceName)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path(URL_RULES_CONTROLLER_PATH + "/{serviceName}/defaultUrlParams")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDefaultUrlParams(@PathParam("serviceName") final String serviceName) {
        if (commonBeans.connector().isConnected()) {
            return Response.ok(extractDefaultUrlParams(serviceName)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path(WHITELISTED_CONTROLLER_PATH + "/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWhitelistedStack(@PathParam("serviceName") String serviceName) {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        return Response.ok(extractWhitelisted(serviceName)).build();
    }


    @GET
    @Path(NAMESPACE_CONTROLLER_PATH + "/getAllNamespacedLists")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllNamespaces() {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.ok(extractAllNamespacedLists()).build();
    }

    @GET
    @Path(NAMESPACE_CONTROLLER_PATH + "getOne/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNamespace(@PathParam("name") final String name) {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        NamespacedList namespacedList = extractNamespacedListByName(name);
        if (namespacedList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(namespacedList).build();
        }
    }
    
    @GET
    @Path(REDIRECTOR_CONTROLLER_PATH + "/" + EndpointPath.APPLICATION_NAMES)
    public Response getApplication() {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        AppNames appNames = new AppNames();
        for (String serviceName : stacksDAO.getAllAppNamesRegisteredInStacks()) {
            appNames.add(serviceName);
        }
        return Response.ok(appNames).build();
    }
    
    @GET
    @Path(REDIRECTOR_CONTROLLER_PATH + "/" + EndpointPath.GET_ALL_REGISTERED_APPS)
    public Response getAllAppNamesRegisteredInStacks() {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        AppNames appNames = new AppNames();
        for (String serviceName : stacksDAO.getAllAppNamesRegisteredInStacks()) {
            appNames.add(serviceName);
        }
        return Response.ok(appNames).build();
    }
    
    @GET
    @Path(STACKS_RELOAD_PATH + "/getVersion/{serviceName}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getStacksVersion(@PathParam("serviceName") String serviceName) {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        return Response.ok(nodeVersionDAO.getStacksReloadNodeVersion(serviceName)).build();
    }
    
    @GET
    @Path(MODEL_RELOAD_PATH + "/getVersion/{serviceName}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getModelVersion(@PathParam("serviceName") String serviceName) {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.ok(nodeVersionDAO.getModelChangedVersion(serviceName, false)).build();
    }
    
    @GET
    @Path(NAMESPACE_CONTROLLER_PATH + "/getVersion/")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getNamespacedListsVersion() {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.ok(nodeVersionDAO.getNamespacedListsVersion(false)).build();
    }
    
    @GET
    @Path(STACKS_CONTROLLER_PATH + "/getVersion/")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getStacksVersion() {
        if (!commonBeans.connector().isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.ok(nodeVersionDAO.getStacksVersion()).build();
    }
    
    @GET
    @Path(INITIALIZER_CONTROLLER_PATH + "/" + VALID_MODEL_EXISTS + "/{appName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response validModelExists(@PathParam("appName") final String appName) {
        return Response.ok(Boolean.TRUE).build();
    }
    
    private Namespaces extractAllNamespacedLists() {
        try {
            Namespaces namespaces = new Namespaces();
            namespaces.setNamespaces(namespacedListDAO.getAll());
            return namespaces;
        } catch (Exception e) {
            log.warn("Failed to get namespacedList: ", e.getMessage());
            return null;
        }
    }

    private NamespacedList extractNamespacedListByName(String name) {
        return namespacedListDAO.getById(name);
    }

    private Default extractDefaultUrlParams(String serviceName) {
        try {
            return new Default(urlParamsDAO.getById(serviceName, "default"));
        } catch (Exception e) {
            log.warn("Failed to get defaultUrlParams: ", e.getMessage());
            return null;
        }
    }

    private SelectServer extractFlavorRulesService(String serviceName) {
        try {
            SelectServer selectServer = new SelectServer();
            selectServer.setItems(flavorRulesDAO.getAll(serviceName));
            return selectServer;
        } catch (Exception e) {
            log.warn("Failed to get flavorRulesService: ", e.getMessage());
            return null;
        }
    }

    private Server extractServer(String appName) {
        try {
            return serverDAO.getById(appName, RedirectorConstants.DEFAULT_SERVER_NAME);
        } catch (Exception e) {
            log.warn("Failed to get server: ", e.getMessage());
            return null;
        }
    }

    private Distribution extractDistribution(String appName) {
        try {
            return distributionDAO.get(appName);
        } catch (Exception e) {
            log.warn("Failed to get distribution: ", e.getMessage());
            return null;
        }
    }

    private URLRules extractAllUrlRules(String serviceName) {
        try {
            URLRules urRules = new URLRules();
            urRules.setItems(urlRulesDAO.getAll(serviceName));
            return urRules;
        } catch (Exception e) {
            log.warn("Failed to get allUrlRules: ", e.getMessage());
            return null;
        }
    }

    private Whitelisted extractWhitelisted(String serviceName) {
        try {
            return whitelistedDAO.get(serviceName);
        } catch (Exception e) {
            log.warn("Failed to get whitelisted: ", e.getMessage());
            return null;
        }
    }
}
