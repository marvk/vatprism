package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimCountry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class CountryRepository extends SimpleDataRepository<Country, CountryRepository.VatsimCountryWrapper> {

    private final Lookup<Country> prefixLookup;

    @Inject
    public CountryRepository(final VatsimApi vatsimApi) {
        super(vatsimApi);
        this.prefixLookup = Lookup.fromCollection(Country::getPrefixes);
    }

    @Override
    protected Country newViewModelInstance(final VatsimCountryWrapper vatsimCountryWrapper) {
        return new Country();
    }

    @Override
    protected String keyFromModel(final VatsimCountryWrapper vatsimCountryWrapper) {
        return vatsimCountryWrapper.getName();
    }

    @Override
    protected String keyFromViewModel(final Country country) {
        return country.getName();
    }

    @Override
    protected void onAdd(final Country toAdd, final VatsimCountryWrapper vatsimCountryWrapper) {
        prefixLookup.put(toAdd);
    }

    @Override
    protected Collection<VatsimCountryWrapper> extractModels(final VatsimApi api) throws VatsimApiException {
        return api
                .vatSpy()
                .getCountries()
                .stream()
                .collect(Collectors.groupingBy(VatsimCountry::getName))
                .values()
                .stream()
                .map(VatsimCountryWrapper::new)
                .collect(Collectors.toList());
    }

    public Country getByPrefix(final String prefix) {
        final List<Country> countries = prefixLookup.get(prefix);
        if (countries == null || countries.isEmpty()) {
            return null;
        }

        if (countries.size() != 1) {
            log.warn("Found multiple countries for prefix \"%s\"".formatted(prefix));
        }

        return countries.get(0);
    }

    @Value
    static class VatsimCountryWrapper {
        String name;
        List<String> prefixes;
        String discriminator;

        public VatsimCountryWrapper(final List<VatsimCountry> vatsimCountries) {
            this.name = name(vatsimCountries);
            this.prefixes = prefixes(vatsimCountries);
            this.discriminator = discriminator(vatsimCountries);
        }

        private String discriminator(final List<VatsimCountry> vatsimCountries) {
            final List<String> discriminators = vatsimCountries
                    .stream()
                    .map(VatsimCountry::getDiscriminator)
                    .distinct()
                    .collect(Collectors.toList());

            if (discriminators.isEmpty()) {
                return null;
            }

            if (discriminators.size() > 1) {
                log.warn("Countries with matching name \"" + name + "\" have mismatched discriminators: " + discriminators);
            }

            return discriminators.get(0);
        }

        private List<String> prefixes(final List<VatsimCountry> vatsimCountries) {
            return vatsimCountries
                    .stream()
                    .map(VatsimCountry::getShorthand)
                    .distinct()
                    .collect(Collectors.toList());
        }

        private String name(final List<VatsimCountry> vatsimCountries) {
            final List<String> names = vatsimCountries
                    .stream()
                    .map(VatsimCountry::getName)
                    .distinct()
                    .collect(Collectors.toList());

            if (names.size() != 1) {
                log.warn("Countries passed to VatsimCountryWrapper have mismatched names: " + names);
            }

            return names.get(0);
        }
    }
}
