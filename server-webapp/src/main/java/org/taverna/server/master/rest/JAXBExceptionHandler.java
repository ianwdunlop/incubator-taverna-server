package org.taverna.server.master.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.taverna.server.master.TavernaServerImpl.log;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

@Provider
public class JAXBExceptionHandler extends HandlerCore implements
		ExceptionMapper<JAXBException> {
	@Override
	public Response toResponse(JAXBException exn) {
		if (managementModel.getLogOutgoingExceptions())
			log.info("converting exception to response", exn);
		return Response.status(FORBIDDEN).type(TEXT_PLAIN_TYPE).entity(
				"APIEpicFail: " + exn.getErrorCode() + "\n" + exn.getMessage())
				.build();
	}
}
