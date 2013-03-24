package nl.runnable.alfresco.extensions;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Provides metadata on a Dynamic Extension and its contents.
 * 
 * @author Laurens Fridael
 * 
 */
public class Extension implements Comparable<Extension> {

	private static final int FRAMEWORK_BUNDLE_ID = 0;

	/* Dependencies */

	private ExtensionRegistry extensionRegistry;

	/* State */

	private long bundleId;

	private String name;

	private String version;

	private Date createdAt;

	private final Map<QName, Model> modelsByQName = new LinkedHashMap<QName, Model>();

	/* Dependencies */

	public void setExtensionRegistry(final ExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}

	protected ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	/* State */

	public void setBundleId(final long bundleId) {
		this.bundleId = bundleId;
	}

	public long getBundleId() {
		return bundleId;
	}

	public boolean isCoreBundle() {
		boolean coreBundle = false;
		if (getExtensionRegistry() != null) {
			coreBundle = getExtensionRegistry().isCoreBundle(getBundleId());
		}
		return coreBundle;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void registerModel(final QName qName, final Model model) {
		modelsByQName.put(qName, model);
	}

	public void unregisterModel(final QName qName) {
		modelsByQName.remove(qName);
	}

	public Collection<Model> getModels() {
		return modelsByQName.values();
	}

	/* Lifecycle */

	public void register() {
		getExtensionRegistry().registerExtension(this);
	}

	public void unregister() {
		getExtensionRegistry().unregisterExtension(this);
	}

	/* Equality */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Extension other = (Extension) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

	/* Comparison */

	@Override
	public int compareTo(final Extension other) {
		// Framework bundle should be put at the start.
		if (this.getBundleId() == FRAMEWORK_BUNDLE_ID) {
			return Integer.MIN_VALUE;
		} else if (other.getBundleId() == FRAMEWORK_BUNDLE_ID) {
			return Integer.MAX_VALUE;

		}
		int compare = this.getName().compareToIgnoreCase(other.getName());
		if (compare == 0) {
			compare = this.getVersion().compareToIgnoreCase(other.getVersion());
		}
		return compare;
	}

}
