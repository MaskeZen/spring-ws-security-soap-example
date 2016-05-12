/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2015 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.wandrell.example.swss.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

import com.wandrell.example.swss.generated.entity.Entity;
import com.wandrell.example.swss.generated.entity.GetEntityRequest;
import com.wandrell.example.swss.generated.entity.GetEntityResponse;
import com.wandrell.example.swss.model.ExampleEntity;
import com.wandrell.example.swss.service.domain.ExampleEntityService;

/**
 * Endpoint for the example entities.
 * <p>
 * It just receives a request with the id for a single entity and then returns a
 * response containing that same entity's data.
 * <p>
 * For both of them the JAXB annotated classes generated from the XSD file,the
 * same file used to create the WSDL for the endpoints, are used.
 * <p>
 * The project is based around securing endpoints, but by default this one is
 * unsecured. This concern is handled by Spring, not by the class itself.
 * <p>
 * For this same reason the endpoint is annotated with Spring annotations, and
 * meant to be wired by this framework.
 *
 * @author Bernardo Martínez Garrido
 * @see GetEntityResponse
 * @see GetEntityRequest
 * @see Entity
 */
@Endpoint
public class ExampleEntityEndpoint {

    /**
     * The logger used for logging the endpoint.
     */
    private static final Logger        LOGGER = LoggerFactory
            .getLogger(ExampleEntityEndpoint.class);

    /**
     * Domain service for accessing the entities handled by the web service.
     * <p>
     * These entities are not the ones generated by JAXB from the XSD, but the
     * actual domain model classes.
     * <p>
     * This is injected by Spring.
     */
    private final ExampleEntityService entityService;

    /**
     * Constructs an endpoint for the example entities using the specified
     * domain service.
     *
     * @param service
     *            the service for the domain entities
     */
    @Autowired
    public ExampleEntityEndpoint(final ExampleEntityService service) {
        super();

        entityService = checkNotNull(service,
                "Received a null pointer as service");
    }

    /**
     * Receives a query for an entity and returns the data for said entity.
     * <p>
     * The data to be returned will be acquired from the persistence layer, and
     * then set into the response.
     * <p>
     * But the domain model classes won't be returned. Instead JAXB beans,
     * generated from the XSD file, will be used both for mapping the SOAP
     * request payload, and to generate the SOAP response payload.
     *
     * @param request
     *            payload of the SOAP request for the entity
     * @return payload for the SOAP response with the entity
     */
    @PayloadRoot(localPart = ExampleEntityEndpointConstants.REQUEST,
            namespace = ExampleEntityEndpointConstants.ENTITY_NS)
    @SoapAction(ExampleEntityEndpointConstants.ACTION)
    @ResponsePayload
    public final GetEntityResponse
            getEntity(@RequestPayload final GetEntityRequest request) {
        final GetEntityResponse response; // SOAP response with the result
        final ExampleEntity entity;       // Found entity
        final Entity entityResponse;      // Entity to return

        checkNotNull(request, "Received a null pointer as request");

        LOGGER.debug(
                String.format("Received request for id %d", request.getId()));

        // Acquires the entity
        entity = getExampleEntityService().findById(request.getId());

        // The entity is transformed from the domain model to the JAXB model
        entityResponse = new Entity();
        BeanUtils.copyProperties(entity, entityResponse);

        LOGGER.debug(String.format("Found entity with id %1$d and name %2$s",
                entity.getId(), entity.getName()));

        response = new GetEntityResponse();
        response.setEntity(entityResponse);

        return response;
    }

    /**
     * Returns the entity domain service.
     * <p>
     * These entities are not the ones generated by JAXB from the XSD, but the
     * actual domain model classes.
     *
     * @return the entity domain service
     */
    private final ExampleEntityService getExampleEntityService() {
        return entityService;
    }

}