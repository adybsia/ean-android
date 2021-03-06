/*
 * Copyright (c) 2013, Expedia Affiliate Network
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that redistributions of source code
 * retain the above copyright notice, these conditions, and the following
 * disclaimer. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Expedia Affiliate Network or Expedia Inc.
 */

package com.ean.mobile.hotel.request;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.ean.mobile.CustomerAddress;
import com.ean.mobile.JSONFileUtil;
import com.ean.mobile.TestConstants;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.ConfirmationStatus;
import com.ean.mobile.hotel.Itinerary;
import com.ean.mobile.hotel.NightlyRate;
import com.ean.mobile.hotel.Rate;
import com.ean.mobile.hotel.SupplierType;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.DateModifier;
import com.ean.mobile.request.RequestTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ItineraryRequestTest extends RequestTestBase {

    private ItineraryRequest itineraryRequest;

    @Before
    public void setUp() {
        super.setUp();
        CommonParameters.customerSessionId = TestConstants.CUSTOMER_SESSION_ID;
        itineraryRequest = new ItineraryRequest(1234L, "test@expedia.com");
    }

    @Test
    public void testConsumeNullJson() throws Exception {
        assertNull(itineraryRequest.consume(null));
    }

    @Test(expected = JSONException.class)
    public void testConsumeEmptyJson() throws Exception {
        itineraryRequest.consume(new JSONObject());
    }

    @Test(expected = JSONException.class)
    public void testConsumeInvalidJson() throws Exception {
        itineraryRequest.consume(JSONFileUtil.loadJsonFromFile("invalid-itinerary.json"));
    }

    @Test(expected = EanWsError.class)
    public void testConsumeEanWsError() throws Exception {
        itineraryRequest.consume(JSONFileUtil.loadJsonFromFile("error-itinerary.json"));
    }

    @Test
    public void testConsume() throws Exception {
        Itinerary itinerary = itineraryRequest.consume(JSONFileUtil.loadJsonFromFile("valid-itinerary.json"));
        assertNotNull(itinerary);
        assertEquals(107730857L, itinerary.id);
        assertEquals(CommonParameters.cid, String.valueOf(itinerary.affiliateId));
        assertEquals(DateModifier.getDateFromString("01/28/2013"), itinerary.creationDate);
        assertEquals(DateModifier.getDateFromString("02/07/2013"), itinerary.itineraryStartDate);
        assertEquals(DateModifier.getDateFromString("02/10/2013"), itinerary.itineraryEndDate);

        doCustomerAssertions(itinerary.customer);
        doHotelConfirmationAssertions(itinerary.hotelConfirmations);
    }

    @Test
    public void testGetUri() throws Exception {
        StringBuilder queryString = buildBaseQueryString();
        queryString.append("&itineraryId=1234&email=test@expedia.com");

        final URI uri = itineraryRequest.getUri();
        assertEquals("http", uri.getScheme());
        assertEquals("api.ean.com", uri.getHost());
        assertEquals("/ean-services/rs/hotel/v3/itin", uri.getPath());
        assertEquals(queryString.toString(), uri.getQuery());
    }

    @Test
    public void testIsSecure() {
        assertThat(itineraryRequest.requiresSecure(), is(false));
    }

    private void doCustomerAssertions(final Itinerary.Customer customer) {
        assertEquals("test", customer.name.first);
        assertEquals("tester", customer.name.last);
        assertEquals("test@expedia.com", customer.email);
        assertEquals("1234567890", customer.homePhone);

        CustomerAddress customerAddress = customer.address;
        assertNotNull(customerAddress);
        assertNotNull(customerAddress.lines);
        assertThat(customerAddress.lines.size(), greaterThan(0));
        assertEquals("travelnow", customerAddress.lines.get(0));
        assertEquals("Seattle", customerAddress.city);
        assertEquals("WA", customerAddress.stateProvinceCode);
        assertEquals("98004", customerAddress.postalCode);
        assertEquals(1, customerAddress.type.typeId);
        assertThat(customerAddress.isPrimary, is(true));
    }

    private void doHotelConfirmationAssertions(final List<Itinerary.HotelConfirmation> hotelConfirmations) {
        assertNotNull(hotelConfirmations);
        assertThat(hotelConfirmations.size(), greaterThan(0));

        Itinerary.HotelConfirmation hotelConfirmation = hotelConfirmations.get(0);
        assertNotNull(hotelConfirmation);
        assertEquals("7220", hotelConfirmation.roomTypeCode);
        assertEquals(SupplierType.EXPEDIA, hotelConfirmation.supplierType);
        assertEquals(ConfirmationStatus.CONFIRMED, hotelConfirmation.status);
        assertEquals(DateModifier.getDateFromString("02/07/2013"), hotelConfirmation.arrivalDate);
        assertEquals(DateModifier.getDateFromString("02/10/2013"), hotelConfirmation.departureDate);
        assertEquals(CommonParameters.locale, hotelConfirmation.locale);
        assertEquals("1234", hotelConfirmation.confirmationNumber);
        assertEquals("N", hotelConfirmation.smokingPreference);
        assertEquals("7220", hotelConfirmation.rateCode);
        assertEquals(1, hotelConfirmation.occupancy.numberOfAdults);
        assertThat(hotelConfirmation.occupancy.childAges.size(), equalTo(0));
        assertEquals("7-Day Advance Purchase Special (on select nights)", hotelConfirmation.rateDescription);
        assertEquals("EP", hotelConfirmation.chainCode);
        assertEquals(3, hotelConfirmation.nights);
        assertEquals("Queen of Art", hotelConfirmation.roomDescription);
        assertEquals("test", hotelConfirmation.guestName.first);
        assertEquals("tester", hotelConfirmation.guestName.last);

        doRateAssertions(hotelConfirmation.rate);
    }

    private void doRateAssertions(final Rate rate) {
        assertNotNull(rate);
        assertThat(rate.promo, is(false));

        Rate.RateInformation chargeableRateInformation = rate.chargeable;
        assertNotNull(chargeableRateInformation);
        assertEquals(new BigDecimal("79.96"), chargeableRateInformation.getSurchargeTotal());
        assertEquals(new BigDecimal("509.96"), chargeableRateInformation.getTotal());
        assertEquals(new BigDecimal("143.33"), chargeableRateInformation.getAverageBaseRate());
        assertEquals(new BigDecimal("143.33"), chargeableRateInformation.getAverageRate());
        assertEquals(CommonParameters.currencyCode, chargeableRateInformation.currencyCode);

        assertNotNull(chargeableRateInformation.nightlyRates);
        assertThat(chargeableRateInformation.nightlyRates.size(), equalTo(3));

        NightlyRate nightlyRate = chargeableRateInformation.nightlyRates.get(0);
        assertNotNull(nightlyRate);
        assertEquals(new BigDecimal("169.0"), nightlyRate.baseRate);
        assertEquals(new BigDecimal("169.0"), nightlyRate.rate);
        assertThat(nightlyRate.promo, is(false));

        nightlyRate = chargeableRateInformation.nightlyRates.get(1);
        assertNotNull(nightlyRate);
        assertEquals(new BigDecimal("126.75"), nightlyRate.baseRate);
        assertEquals(new BigDecimal("126.75"), nightlyRate.rate);
        assertThat(nightlyRate.promo, is(false));

        nightlyRate = chargeableRateInformation.nightlyRates.get(2);
        assertNotNull(nightlyRate);
        assertEquals(new BigDecimal("134.25"), nightlyRate.baseRate);
        assertEquals(new BigDecimal("134.25"), nightlyRate.rate);
        assertThat(nightlyRate.promo, is(false));
    }

}