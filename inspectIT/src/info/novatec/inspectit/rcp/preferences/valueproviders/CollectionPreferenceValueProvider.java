package info.novatec.inspectit.rcp.preferences.valueproviders;

import info.novatec.inspectit.rcp.preferences.PreferenceException;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory.PreferenceValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;

/**
 * This {@link PreferenceValueProvider} converts any collection that members in primitive warper
 * types to preference value. Later on this preference value will be transformed to a collection of
 * strings, and thus needs transformation to initial class of collection members.
 * 
 * @author Ivan Senic
 * 
 */
public class CollectionPreferenceValueProvider extends PreferenceValueProvider<Collection<?>> {

	/**
	 * Constant for denoting the empty collection.
	 */
	private static final String EMPTY_COLLECTION = "EMPTY_COLLECTION";

	/**
	 * {@inheritDoc}
	 */
	public boolean isObjectValid(Object object) {
		return object instanceof Collection;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueForObject(Collection<?> collection) throws PreferenceException {
		if (CollectionUtils.isEmpty(collection)) {
			return EMPTY_COLLECTION;
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			for (Object object : collection) {
				stringBuilder.append(String.valueOf(object) + PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			}
			return stringBuilder.toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<?> getObjectFromValue(String value) throws PreferenceException {
		if (EMPTY_COLLECTION.equals(value)) {
			return Collections.emptyList();
		} else {
			Collection<String> results = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(value, PreferencesConstants.PREF_OBJECT_SEPARATION_TOKEN);
			while (tokenizer.hasMoreElements()) {
				results.add(tokenizer.nextToken());
			}
			return results;
		}
	}

}
