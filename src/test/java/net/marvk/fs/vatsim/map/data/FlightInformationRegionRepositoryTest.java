package net.marvk.fs.vatsim.map.data;

import lombok.Value;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.api.StringDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FlightInformationRegionRepositoryTest {
    private FlightInformationRegionRepository sut;

    @BeforeEach
    private void setup() throws IOException, RepositoryException {
        final String firBoundaries = loadFile("FIRBoundaries.dat");
        final String vatSpy = loadFile("VATSpy.dat");

        final var ds = new StringDataSource(null, null, firBoundaries, vatSpy, null, null);

        final var api = new SimpleVatsimApi(ds);

        sut = new FlightInformationRegionRepository(api, FlightInformationRegion::new);

        sut.reload();
    }

    private static String loadFile(final String fileName) throws IOException {
        try (final var is = FlightInformationRegionRepositoryTest.class.getResourceAsStream("/net/marvk/fs/vatsim/map/data/" + fileName)) {
            Objects.requireNonNull(is);
            return new String(is.readAllBytes());
        }
    }

    @ParameterizedTest
    @MethodSource("testParameters")
    public void testLondonRegions(final TestParameters testParameters) {
        final String givenIdentifier = testParameters.getGivenIdentifier();
        final String givenInfix = testParameters.getGivenInfix();

        final List<FlightInformationRegion> actual = sut.getByIdentifierAndInfix(givenIdentifier, givenInfix);

        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(testParameters.shouldBePrefixPosition, actual.get(0).getPrefixPosition());
    }

    private static Stream<TestParameters> testParameters() {
        return Stream.of(
                new TestParameters("LON", "S", "LON_S"),
                new TestParameters("LON", "N", "LON_N"),
                new TestParameters("LON", "C", "LON_C")
        );
    }

    ;

    @Value
    static class TestParameters {
        String givenIdentifier;
        String givenInfix;
        String shouldBePrefixPosition;
    }
}

