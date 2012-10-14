/*
Copyright (c) 2012, Runnable
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of Runnable nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nl.runnable.alfresco.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.VersionNumber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Registers beans from an {@link ApplicationContext} as services in a {@link BundleContext}.
 * 
 * @author Laurens Fridael
 * 
 */
@Service
public class ServiceBundleContextRegistrar implements BundleContextRegistrar, ApplicationContextAware {

	/* Service property */
	private static final String ALFRESCO_SERVICE_TYPE = "alfresco.service.type";

	/* Service property for the Blueprint component name. */
	private static final String OSGI_SERVICE_BLUEPRINT_COMPNAME = "osgi.service.blueprint.compname";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/* Dependencies */

	private ApplicationContext applicationContext;

	private DescriptorService descriptorService;

	/* Configuration */

	private List<ServiceDefinition> serviceDefinitions = Collections.emptyList();

	private List<ServicePropertiesProvider> servicePropertiesProviders = Collections.emptyList();

	private final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();

	private boolean verifyServicesImplementInterfaces = true;

	/* Operations */

	@Override
	public List<ServiceRegistration<?>> registerInBundleContext(final BundleContext bundleContext) {
		Assert.notNull(bundleContext, "BundleContext cannot be null.");

		final Descriptor descriptor = getDescriptorService().getServerDescriptor();
		if (descriptor != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not obtain Descriptor. This is normal when running an integration test.");
			}
		}

		for (final ServiceDefinition serviceDefinition : getServiceDefinitions()) {
			int serviceRanking = 0;
			for (final String beanName : serviceDefinition.getBeanNames()) {
				final List<String> serviceNames = serviceDefinition.getServiceNames();
				final String requiredPlatformVersion = serviceDefinition.getPlatformVersion();
				if (StringUtils.hasText(requiredPlatformVersion)) {
					final VersionNumber versionNumber = new VersionNumber(requiredPlatformVersion);
					if (descriptor != null && versionNumber.compareTo(descriptor.getVersionNumber()) > 0) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Skipping registration of ServiceDefinitions {} because they require Alfresco version {}",
									serviceNames, requiredPlatformVersion);
						}
						continue;
					}
				}
				if (getApplicationContext().containsBean(beanName) == false) {
					logger.warn(
							"Could not find service \"{}\". This service will not be registered in the OSGI container.",
							beanName);
					continue;
				}
				if (getApplicationContext().isSingleton(beanName) == false) {
					logger.warn(String.format("Service \"{}\" is not a singleton. Can only register singleton beans.",
							beanName));
					continue;
				}
				final Object service = getApplicationContext().getBean(beanName);
				if (isVerifyServicesImplementInterfaces()
						&& verifyServiceImplementsInterfaces(service, serviceNames) == false) {
					continue;
				}
				try {
					final ServiceRegistration<?> serviceRegistration = registerService(bundleContext, service,
							serviceNames, beanName, serviceDefinition.getServiceType(), serviceRanking);
					serviceRanking -= 1;
					serviceRegistrations.add(serviceRegistration);
				} catch (final RuntimeException e) {
					logger.error("Could not register bean \"{} \" as service", beanName);
				}
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("Registered {} OSGI services.", serviceRegistrations.size());
		}
		return Collections.unmodifiableList(serviceRegistrations);
	}

	protected boolean verifyServiceImplementsInterfaces(final Object service, final List<String> interfaceNames) {
		Assert.notNull(service, "Service cannot be null.");
		Assert.notEmpty(interfaceNames, "Interface names cannot be empty.");

		for (final String serviceName : interfaceNames) {
			try {
				final Class<?> interfaceClass = Class.forName(serviceName);
				if (interfaceClass.isInterface() == false) {
					logger.warn("Service name is not an interface \"{}\". " + "Skipping registration of this service.",
							serviceName);
					return false;
				}
				if (interfaceClass.isInstance(service) == false) {
					logger.warn("Service does not implement the interface \"{}\". "
							+ "Skipping registration of this service.", serviceName);
					return false;
				}
			} catch (final ClassNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	protected ServiceRegistration<?> registerService(final BundleContext bundleContext, final Object service,
			final List<String> serviceNames, final String beanName, final String serviceType, final int serviceRanking) {
		Assert.notNull(service, "Service cannot be null.");
		Assert.notEmpty(serviceNames, "Service names cannot be empty.");

		final Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
		serviceProperties.put(OSGI_SERVICE_BLUEPRINT_COMPNAME, beanName);
		serviceProperties.put("hostApplication", "alfresco");
		if (StringUtils.hasText(serviceType)) {
			serviceProperties.put(ALFRESCO_SERVICE_TYPE, serviceType);
		}
		serviceProperties.put(Constants.SERVICE_RANKING, serviceRanking);
		for (final ServicePropertiesProvider servicePropertiesProvider : getServicePropertiesProviders()) {
			final Map<String, Object> props = servicePropertiesProvider.getServiceProperties(service, serviceNames);
			if (CollectionUtils.isEmpty(props) == false) {
				serviceProperties.putAll(props);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Registering bean \"{}\" under service names {} with properties {}", new Object[] { beanName,
					serviceNames, serviceProperties });
		}
		final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(
				serviceNames.toArray(new String[serviceNames.size()]), service, serviceProperties);
		return serviceRegistration;
	}

	public void unregisterFromBundleContext() {
		for (final ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Unregistering service: {}",
							serviceRegistration.getReference().getProperty(Constants.OBJECTCLASS));
				}
				serviceRegistration.unregister();
			} catch (final RuntimeException e) {
				logger.error("Error unregistering service.", e);
			}
		}
		serviceRegistrations.clear();
	}

	/* Dependencies */

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setDescriptorService(final DescriptorService descriptorService) {
		this.descriptorService = descriptorService;
	}

	protected DescriptorService getDescriptorService() {
		return descriptorService;
	}

	/* Configuration */

	@Required
	public void setServiceDefinitions(final List<ServiceDefinition> serviceDefinitions) {
		Assert.notNull(serviceDefinitions);
		this.serviceDefinitions = serviceDefinitions;
	}

	protected List<ServiceDefinition> getServiceDefinitions() {
		return serviceDefinitions;
	}

	public void setServicePropertiesProviders(final List<ServicePropertiesProvider> servicePropertiesProviders) {
		Assert.notNull(servicePropertiesProviders);
		this.servicePropertiesProviders = servicePropertiesProviders;
	}

	protected List<ServicePropertiesProvider> getServicePropertiesProviders() {
		return servicePropertiesProviders;
	}

	public void setVerifyServicesImplementInterfaces(final boolean verifyServicesImplementInterfaces) {
		this.verifyServicesImplementInterfaces = verifyServicesImplementInterfaces;
	}

	protected boolean isVerifyServicesImplementInterfaces() {
		return verifyServicesImplementInterfaces;
	}

}