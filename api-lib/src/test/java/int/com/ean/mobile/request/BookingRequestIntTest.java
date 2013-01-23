/*
 * Copyright 2013 EAN.com, L.P. All rights reserved.
 */

package com.ean.mobile.request;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.junit.Test;

import com.ean.mobile.Address;
import com.ean.mobile.BasicAddress;
import com.ean.mobile.HotelInfoList;
import com.ean.mobile.HotelRoom;
import com.ean.mobile.Name;
import com.ean.mobile.Reservation;
import com.ean.mobile.ReservationRoom;
import com.ean.mobile.RoomOccupancy;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BookingRequestIntTest {

    private static final RoomOccupancy OCCUPANCY = new RoomOccupancy(1, null);

    private static final Address ADDRESS = new BasicAddress("travelnow", "Seattle", "WA", "US", "98004");

    @Test
    public void testBookSingularRoomInSeattle() throws Exception {
        LocalDate[] dateTimes = DateModifier.getAnArrayOfLocalDatesWithOffsets(10, 13);
        ListRequest hotelListRequest = new ListRequest("Seattle, WA", OCCUPANCY,
            dateTimes[0], dateTimes[1], null, "en_US", "USD");
        HotelInfoList hotelList = RequestProcessor.run(hotelListRequest);

        RoomAvailRequest roomAvailRequest = new RoomAvailRequest(hotelList.hotelInfos.get(0).hotelId, OCCUPANCY,
            dateTimes[0], dateTimes[1], hotelList.customerSessionId, "en_US", "USD");

        List<HotelRoom> rooms = RequestProcessor.run(roomAvailRequest);
        BookingRequest.ReservationInfo resInfo = new BookingRequest.ReservationInfo(
            "test@expedia.com", "test", "tester", "1234567890",
            null, "CA", "5401999999999999", "123", YearMonth.now().plusYears(1));

        ReservationRoom room = new ReservationRoom(
            resInfo.individual.name, rooms.get(0), rooms.get(0).bedTypes.get(0).id, OCCUPANCY);

        BookingRequest bookingRequest = new BookingRequest(
            hotelList.hotelInfos.get(0).hotelId, dateTimes[0], dateTimes[1],
            hotelList.hotelInfos.get(0).supplierType, room, resInfo, ADDRESS,
            hotelList.customerSessionId, "en_US", "USD");

        Reservation reservation = RequestProcessor.run(bookingRequest);
        // TODO: some assertions here on hotel/date/occupancy, etc.
        assertEquals((Long) 1234L, reservation.confirmationNumbers.get(0));
    }

    @Test
    public void testBookMultiRoomInSeattle() throws Exception {
        LocalDate[] dateTimes = DateModifier.getAnArrayOfLocalDatesWithOffsets(10, 13);
        List<RoomOccupancy> occupancies = Arrays.asList(OCCUPANCY, new RoomOccupancy(1, 3));

        ListRequest listRequest = new ListRequest("Seattle, WA", occupancies,
            dateTimes[0], dateTimes[1], null, "en_US", "USD");
        HotelInfoList hotelList = RequestProcessor.run(listRequest);

        RoomAvailRequest roomAvailRequest = new RoomAvailRequest(hotelList.hotelInfos.get(0).hotelId,
            occupancies, dateTimes[0], dateTimes[1], hotelList.customerSessionId, "en_US", "USD");
        List<HotelRoom> rooms = RequestProcessor.run(roomAvailRequest);

        List<Name> checkInNames = Arrays.asList(new Name("test", "tester"), new Name("test", "testerson"));

        BookingRequest.ReservationInfo resInfo = new BookingRequest.ReservationInfo("test@expedia.com",
            checkInNames.get(0).first, checkInNames.get(0).last, "1234567890",
            null, "CA", "5401999999999999", "123", YearMonth.now().plusYears(1));

        List<ReservationRoom> bookingRooms = Arrays.asList(
            new ReservationRoom(
                checkInNames.get(0), rooms.get(0), rooms.get(0).bedTypes.get(0).id, occupancies.get(0)),
            new ReservationRoom(
                checkInNames.get(1), rooms.get(0), rooms.get(0).bedTypes.get(0).id, occupancies.get(1)));

        BookingRequest bookingRequest = new BookingRequest(
            hotelList.hotelInfos.get(0).hotelId, dateTimes[0], dateTimes[1],
            hotelList.hotelInfos.get(0).supplierType, bookingRooms, resInfo, ADDRESS,
            hotelList.customerSessionId, "en_US", "USD");
        Reservation reservation = RequestProcessor.run(bookingRequest);

        // TODO: some assertions here on hotel/date/occupancy, etc.
        assertEquals(occupancies.size(), reservation.confirmationNumbers.size());
        assertThat(reservation.confirmationNumbers, hasItems(1234L, 1234L));
    }
}
