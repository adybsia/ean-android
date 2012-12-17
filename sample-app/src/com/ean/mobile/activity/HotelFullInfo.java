/**
 * $Copyright: Copyright 2012 EAN.com, L.P. All rights reserved. $
 */
package com.ean.mobile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.ean.mobile.Constants;
import com.ean.mobile.HotelImageTuple;
import com.ean.mobile.HotelInfo;
import com.ean.mobile.HotelInfoExtended;
import com.ean.mobile.R;
import com.ean.mobile.HotelRoom;
import com.ean.mobile.SampleApp;
import com.ean.mobile.SampleConstants;
import com.ean.mobile.StarRating;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.request.InformationRequest;
import com.ean.mobile.request.RoomAvailRequest;
import com.ean.mobile.task.ImageTupleLoaderTask;
import org.joda.time.DateTime;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

public class HotelFullInfo extends Activity {
    public void onResume() {
        super.onResume();
        setContentView(R.layout.hotelfullinfo);
        Log.d(SampleConstants.DEBUG, "starting HotelFullInfo");
        final HotelInfo hotelInfo = SampleApp.selectedHotel;

        if(hotelInfo == null) {
            Log.d(SampleConstants.DEBUG, "hotel info was null");
            return;
        }

        TextView name = (TextView) this.findViewById(R.id.hotelFullInfoName);
        name.setText(hotelInfo.name);

        StarRating.populate((LinearLayout) this.findViewById(R.id.hotelFullInfoStars), hotelInfo.starRating);

        if(SampleApp.HOTEL_ROOMS.containsKey(hotelInfo.hotelId)) {
            populateRateList();
        } else {
            new AvailabilityInformationLoaderTask(
                    hotelInfo.hotelId,
                    SampleApp.numberOfAdults,
                    SampleApp.numberOfChildren,
                    SampleApp.arrivalDate,
                    SampleApp.departureDate,
                    SampleApp.foundHotels.customerSessionId).execute((Void) null);
        }


        final ImageView thumb = (ImageView) this.findViewById(R.id.hotelFullInfoThumb);
        if (hotelInfo.mainHotelImageTuple.thumbnailUrl != null) {
            if (hotelInfo.mainHotelImageTuple.isThumbnailLoaded()){
                thumb.setImageDrawable(hotelInfo.mainHotelImageTuple.getThumbnailImage());
            } else {
                new ImageTupleLoaderTask(thumb, false).execute(hotelInfo.mainHotelImageTuple);
            }
        }




        if(SampleApp.EXTENDED_INFOS.containsKey(hotelInfo.hotelId)){
            setExtendedInfoFields();
        } else {
            new ExtendedInformationLoaderTask(hotelInfo.hotelId).execute((Void) null);
        }
    }

    private void setExtendedInfoFields() {
        final WebView description = (WebView) this.findViewById(R.id.hotelFullInfoDescription);
        final TextView address = (TextView) this.findViewById(R.id.hotelFullInfoAddress);
        final ImageView[] smallThumbs = {
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb00),
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb01),
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb02),
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb10),
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb11),
                (ImageView) this.findViewById(R.id.hotelFullInfoSmallThumb12)
        };

        address.setText(SampleApp.selectedHotel.address);
        HotelInfoExtended extended = SampleApp.EXTENDED_INFOS.get(SampleApp.selectedHotel.hotelId);
        description.loadData(extended.longDescription, "text/html", null);
        for(int i = 0; i < smallThumbs.length && i < extended.images.size(); i++){
            HotelImageTuple thisImage = extended.images.get(i);
            if (thisImage.isThumbnailLoaded()) {
                smallThumbs[i].setImageDrawable(thisImage.getThumbnailImage());
            } else {
                new ImageTupleLoaderTask(smallThumbs[i], false).execute(thisImage);
            }
        }
    }

    private class AvailabilityInformationLoaderTask extends AsyncTask<Void, Void, List<HotelRoom>> {
        private final long hotelId;
        private final int numberOfAdults;
        private final int numberOfChildren;
        private final DateTime arrivalDate;
        private final DateTime departureDate;
        private final String customerSessionId;

        public AvailabilityInformationLoaderTask(final long hotelId,
                                                 final int numberOfAdults,
                                                 final int numberOfChildren,
                                                 final DateTime arrivalDate,
                                                 final DateTime departureDate,
                                                 final String customerSessionId) {
            this.hotelId = hotelId;
            this.numberOfAdults = numberOfAdults;
            this.numberOfChildren = numberOfChildren;
            this.arrivalDate = arrivalDate;
            this.departureDate = departureDate;
            this.customerSessionId = customerSessionId;
        }
        
        
        @Override
        protected List<HotelRoom> doInBackground(Void... voids) {
            try {
                return RoomAvailRequest.getRoomAvail(
                    hotelId,
                    numberOfAdults,
                    numberOfChildren,
                    arrivalDate,
                    departureDate,
                    customerSessionId);
            } catch (IOException ioe) {
                Log.d(SampleConstants.DEBUG, "An error occurred when performing request.", ioe);
            } catch (EanWsError ewe) {
                Log.d(SampleConstants.DEBUG, "An error occurred in the api", ewe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<HotelRoom> hotelRooms) {
            super.onPostExecute(hotelRooms);
            SampleApp.HOTEL_ROOMS.put(hotelId, hotelRooms);
            populateRateList();
        }
    }

    private class ExtendedInformationLoaderTask extends AsyncTask<Void, Integer, HotelInfoExtended> {

        private final long hotelId;

        public ExtendedInformationLoaderTask(long hotelId) {
            this.hotelId = hotelId;
        }

        @Override
        protected HotelInfoExtended doInBackground(Void... voids) {
            try {
                return InformationRequest.getHotelInformation(hotelId, SampleApp.foundHotels.customerSessionId);
            } catch (IOException ioe) {
                Log.d(SampleConstants.DEBUG, "An ioerror occurred when performing information request", ioe);
            } catch (EanWsError ewe) {
                Log.d(SampleConstants.DEBUG, "Unexpected error occurred within the api", ewe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(HotelInfoExtended extended) {
            super.onPostExecute(extended);
            SampleApp.EXTENDED_INFOS.put(hotelId, extended);
            setExtendedInfoFields();
        }
    }

    private void populateRateList(){
        final HotelInfo hotelInfo = SampleApp.selectedHotel;
        TextView loadingView = (TextView) this.findViewById(R.id.loadingRoomsView);
        loadingView.setVisibility(TextView.GONE);
        if (!SampleApp.HOTEL_ROOMS.containsKey(hotelInfo.hotelId)) {
            TextView noAvail = (TextView) this.findViewById(R.id.noRoomsAvailableView);
            noAvail.setVisibility(TextView.VISIBLE);
            return;
        }
        TableLayout rateList = (TableLayout) findViewById(R.id.roomRateList);
        View view;
        LayoutInflater inflater;
        for (HotelRoom room : SampleApp.HOTEL_ROOMS.get(hotelInfo.hotelId)) {
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.roomtypelistlayout, null);

            TextView roomDesc = (TextView) view.findViewById(R.id.roomRateDescritpiton);
            roomDesc.setText(room.description);

            TextView drrPromoText = (TextView) view.findViewById(R.id.drrPromoText);
            drrPromoText.setText(room.promoDescription);

            TextView highPrice = (TextView) view.findViewById(R.id.highPrice);
            TextView lowPrice = (TextView) view.findViewById(R.id.lowPrice);
            ImageView drrIcon = (ImageView) view.findViewById(R.id.drrPromoImg);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            currencyFormat.setCurrency(Currency.getInstance(room.rate.currencyCode));
            lowPrice.setText(currencyFormat.format(room.rate.getAverageRate()));
            if(room.rate.areAverageRatesEqual()){
                highPrice.setVisibility(TextView.GONE);
                drrIcon.setVisibility(ImageView.GONE);
                drrPromoText.setVisibility(ImageView.GONE);
            } else {
                highPrice.setVisibility(TextView.VISIBLE);
                drrIcon.setVisibility(ImageView.VISIBLE);
                drrPromoText.setVisibility(ImageView.VISIBLE);
                highPrice.setText(currencyFormat.format(room.rate.getAverageBaseRate()));
                highPrice.setPaintFlags(highPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            view.setTag(room);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SampleApp.selectedRoom = (HotelRoom) view.getTag();
                    Intent intent = new Intent(HotelFullInfo.this, BookingSummary.class);
                    startActivity(intent);
                }
            });
            rateList.addView(view);
        }
    }
}