/*
 * Copyright 2013 EAN.com, L.P. All rights reserved.
 */

package com.ean.mobile.activity;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ean.mobile.R;
import com.ean.mobile.SampleApp;
import com.ean.mobile.hotel.Reservation;

public class ReservationDisplay extends Activity {

    private static final String DATE_FORMAT_STRING = "MM/dd/yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_STRING);

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.reservationdisplay);

        final Reservation reservationToDisplay = SampleApp.getLatestReservation();

        final TableLayout infoList = (TableLayout) findViewById(R.id.reservationInfoList);
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Itinerary ID", reservationToDisplay.itineraryId));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Confirmation Numbers", TextUtils.join(",", reservationToDisplay.confirmationNumbers)));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Check-In Instructions", reservationToDisplay.checkInInstructions));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Arrival Date", DATE_FORMATTER.print(reservationToDisplay.arrivalDate)));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Departure Date", DATE_FORMATTER.print(reservationToDisplay.departureDate)));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Hotel Name", reservationToDisplay.hotelName));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Hotel Address", reservationToDisplay.hotelAddress.toString()));
        infoList.addView(inflateKeyValue(getLayoutInflater(), "Room Description", reservationToDisplay.roomDescription));

    }

    private View inflateKeyValue(final LayoutInflater inflater, final String key, final Object value) {
        final View view = inflater.inflate(R.layout.reservationinfolistlayout, null);

        final TextView keyView = (TextView) view.findViewById(R.id.reservationinfokey);
        keyView.setText(key);

        final TextView valueView = (TextView) view.findViewById(R.id.reservationinfovalue);
        valueView.setText(value.toString());
        return view;
    }
}