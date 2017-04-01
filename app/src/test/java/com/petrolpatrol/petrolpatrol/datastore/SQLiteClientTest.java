package com.petrolpatrol.petrolpatrol.datastore;

import com.petrolpatrol.petrolpatrol.BuildConfig;
import com.petrolpatrol.petrolpatrol.Randomize;
import com.petrolpatrol.petrolpatrol.model.Brand;
import com.petrolpatrol.petrolpatrol.model.FuelType;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
sdk = 21)
public class SQLiteClientTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static Randomize randomize;

    private SQLiteClient client;

    @BeforeClass
    public static void construct() {
        randomize = new Randomize();
        System.out.println("Random Seed: " + randomize.getSeed());
    }

    @Before
    public void setUp() {
        client = new SQLiteClient(RuntimeEnvironment.application);
        client.resetDatabase();
    }

    @Test
    public void testOpeningAndClosingDatabase() {
        try {
            client.getAllFuelTypes();
            fail();
        } catch (NullPointerException npe) {
        }
        client.open();
        client.getAllFuelTypes();
        client.getAllBrands();
        client.close();
        try {
            client.getAllFuelTypes();
            fail();
        } catch (NullPointerException npe) {
        }
        client.close();
    }

    @Test
    public void testEmptyDatabaseReturnsEmpty() {
        client.open();
        assertTrue(client.getAllBrands().isEmpty());
        assertTrue(client.getAllFuelTypes().isEmpty());
        assertTrue(client.getAllStations().isEmpty());
        assertTrue(client.getAllPrices().isEmpty());
        client.close();
    }

    @Test
    public void testGetAlls() {

        client.open();

        int numBrands = randomize.nextInt(10);
        List<Brand> testBrands = new ArrayList<>();
        for (int i = 0; i < numBrands; i++) {
            testBrands.add(new Brand(randomize.nextString(10)));
            client.insertBrand(testBrands.get(i));
        }

        int n = 0;
        for (Brand b : client.getAllBrands()) {
            assertEquals(testBrands.get(n).getName(), b.getName());
            n++;
        }

        int numFuelTypes = randomize.nextInt(10);
        List<FuelType> testFuelTypes = new ArrayList<>();

        for (int i = 0; i < numFuelTypes; i++) {
            testFuelTypes.add(new FuelType(randomize.nextString(3), randomize.nextString(10)));
            client.insertFuelType(testFuelTypes.get(i));
        }

        n = 0;
        for (FuelType ft : client.getAllFuelTypes()) {
            assertEquals(testFuelTypes.get(n).getCode(), ft.getCode());
            assertEquals(testFuelTypes.get(n).getName(), ft.getName());
            n++;
        }

        int numStations = randomize.nextInt(10);
        List<Station> testStations = new ArrayList<>();
        for (int i = 0; i < numStations; i++) {
            testStations.add(new Station(mock(Brand.class), randomize.nextInt(2000), randomize.nextString(10), randomize.nextString(20), randomize.nextDouble(90), randomize.nextDouble(180)));
            client.insertStation(testStations.get(i));
        }

        assertEquals(testStations.size(), client.getAllStations().size());


        testFuelTypes = client.getAllFuelTypes();
        //int numPrices = numStations * numFuelTypes;
        int l = 0;
        List<Price> testPrices = new ArrayList<>();
        for (int i = 0; i < numStations; i++) {
            for (int j = 0; j < numFuelTypes; j++) {
                testPrices.add(new Price(testStations.get(i).getId(), testFuelTypes.get(j), randomize.nextDouble(200), randomize.nextString(20)));
                client.insertPrice(testPrices.get(l));
                l++;
            }
        }

        assertEquals(testPrices.size(), client.getAllPrices().size());

        client.close();
    }

    @Test
    public void testGetAndSetBrand() {

        String mockedName = randomize.nextString(10);

        client.open();

        Brand brand = new Brand(mockedName);
        client.insertBrand(brand);

        brand = client.getBrand(mockedName);
        int id = brand.getId();
        assertEquals(mockedName, brand.getName());

        brand = client.getBrand(id);
        assertEquals(id, brand.getId());
        assertEquals(mockedName, brand.getName());

        String differentMockName = randomize.nextString(10);

        brand = new Brand(id, differentMockName);
        client.insertBrand(brand);

        brand = client.getBrand(id);
        assertEquals(id, brand.getId());
        assertEquals(differentMockName, brand.getName());

        client.close();
    }

    @Test
    public void testGetAndSetFuelType() {

        String mockedCode = randomize.nextString(3);
        String mockedName = randomize.nextString(10);

        client.open();

        FuelType fuelType = new FuelType(mockedCode, mockedName);
        client.insertFuelType(fuelType);

        fuelType = client.getFuelType(mockedCode);
        int id = fuelType.getId();
        assertEquals(mockedCode, fuelType.getCode());
        assertEquals(mockedName, fuelType.getName());

        fuelType = client.getFuelType(id);
        assertEquals(id, fuelType.getId());
        assertEquals(mockedCode, fuelType.getCode());
        assertEquals(mockedName, fuelType.getName());

        String differentMockCode = randomize.nextString(3);
        String differentMockName = randomize.nextString(10);

        fuelType = new FuelType(id, differentMockCode, differentMockName);
        client.insertFuelType(fuelType);

        fuelType = client.getFuelType(id);
        assertEquals(id, fuelType.getId());
        assertEquals(differentMockCode, fuelType.getCode());
        assertEquals(differentMockName, fuelType.getName());

        client.close();
    }

    @Test
    public void testGetAndSetStation() {

        Brand mockedBrand = mock(Brand.class);
        int mockID = randomize.nextInt(3000);
        String mockedName = randomize.nextString(10);
        String mockedAddress = randomize.nextString(50);
        double mockedLatitude = -randomize.nextDouble(90);
        double mockedLongitude = randomize.nextDouble(180);
        double mockedDistance = randomize.nextDouble(1000);

        client.open();

        Station station = new Station(mockedBrand, mockID, mockedName, mockedAddress, mockedLatitude, mockedLongitude, mockedDistance);
        client.insertStation(station);

        station = client.getStation(mockID);
        assertEquals(mockID, station.getId());
        assertEquals(mockedName, station.getName());
        assertEquals(mockedAddress, station.getAddress());
        assertEquals(mockedLatitude, station.getLatitude(), Double.MIN_VALUE);
        assertEquals(mockedLongitude, station.getLongitude(), Double.MIN_VALUE);
        assertEquals(Station.NO_DISTANCE, station.getDistance(), Double.MIN_VALUE);

        String differentMockedName = randomize.nextString(10);
        String differentMockedAddress = randomize.nextString(50);
        double differentMockedLatitude = -randomize.nextDouble(90);
        double differentMockedLongitude = randomize.nextDouble(180);
        double differentMockedDistance = randomize.nextDouble(1000);

        station = new Station(mockedBrand, mockID, differentMockedName, differentMockedAddress, differentMockedLatitude, differentMockedLongitude, differentMockedDistance);
        client.insertStation(station);

        station = client.getStation(mockID);
        assertEquals(mockID, station.getId());
        assertEquals(differentMockedName, station.getName());
        assertEquals(differentMockedAddress, station.getAddress());
        assertEquals(differentMockedLatitude, station.getLatitude(), Double.MIN_VALUE);
        assertEquals(differentMockedLongitude, station.getLongitude(), Double.MIN_VALUE);
        assertEquals(Station.NO_DISTANCE, station.getDistance(), Double.MIN_VALUE);

        client.close();
    }

    @Test
    public void testGetAndSetPrice() {

        client.open();

        Brand mockedBrand = mock(Brand.class);
        int mockID = randomize.nextInt(3000);
        String mockedName = randomize.nextString(10);
        String mockedAddress = randomize.nextString(50);
        double mockedLatitude = -randomize.nextDouble(90);
        double mockedLongitude = randomize.nextDouble(180);
        double mockedDistance = randomize.nextDouble(1000);

        Station station = new Station(mockedBrand, mockID, mockedName, mockedAddress, mockedLatitude, mockedLongitude, mockedDistance);

        int numPrices = 5;

        List<FuelType> mockFuelTypes = new ArrayList<>(numPrices);
        for (int i = 0; i < numPrices; i++) {
            mockFuelTypes.add(new FuelType(randomize.nextString(3), randomize.nextString(10)));
            client.insertFuelType(mockFuelTypes.get(i));
        }
        mockFuelTypes = client.getAllFuelTypes(); // fill in dbid of fueltype

        List<Price> testPrices = new ArrayList<>(numPrices);
        for (int i = 0; i < numPrices; i++) {
            testPrices.add(new Price(mockID, mockFuelTypes.get(i), randomize.nextDouble(200), randomize.nextString(20)));
            station.setPrice(testPrices.get(i));
        }

        client.insertStation(station);

        station = client.getStation(mockID);
        for (int i = 0; i < numPrices; i++) {
            Price price = station.getPrice(mockFuelTypes.get(i).getCode());
            assertEquals(testPrices.get(i).getFuelType().getId(), price.getFuelType().getId());
            assertEquals(testPrices.get(i).getPrice(), price.getPrice(), Double.MIN_VALUE);
            assertEquals(testPrices.get(i).getStationID(), station.getId());
            assertEquals(testPrices.get(i).getLastUpdated(), price.getLastUpdated());
        }

        client.close();
    }
}