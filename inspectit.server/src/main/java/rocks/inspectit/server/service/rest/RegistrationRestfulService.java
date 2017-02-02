package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.server.service.rest.model.RegisterBody;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Marius Oehler
 *
 */
@Controller
@RequestMapping(value = "/data/registration")
public class RegistrationRestfulService {

	@Autowired
	private IRegistrationService registrationService;

	/**
	 * Handling of all the exceptions happening in this controller.
	 *
	 * @param exception
	 *            Exception being thrown
	 * @return {@link ModelAndView}
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception exception) {
		return new JsonError(exception).asModelAndView();
	}

	@RequestMapping(method = POST, value = "register")
	@ResponseBody
	public ResponseEntity<Long> getInvocationSequenceDetails(@RequestBody RegisterBody registerBody) throws BusinessException {
		long agentId = registrationService.registerPlatformIdent(registerBody.getIps(), registerBody.getName(), registerBody.getVersion());
		return new ResponseEntity<>(agentId, HttpStatus.OK);
	}
}
