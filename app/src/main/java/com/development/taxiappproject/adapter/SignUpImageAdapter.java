//package com.development.taxiappproject.adapter;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.development.taxiappproject.R;
//import com.development.taxiappproject.model.SignUpImageModel;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.Objects;
//
////public class SignUpImageAdapter {
////}
//public class SignUpImageAdapter extends ArrayAdapter<SignUpImageModel> {
//
//    Context context;
//    List<SignUpImageModel> myList;
//
//    public SignUpImageAdapter(Context context, int resource, List<SignUpImageModel> objects) {
//        super(context, resource, objects);
//
//        this.context = context;
//        this.myList = objects;
//    }
//
//
//    @Override
//    public int getCount() {
//        if (myList != null)
//            return myList.size();
//        return 0;
//    }
//
//    @Override
//    public SignUpImageModel getItem(int position) {
//        if (myList != null)
//            return myList.get(position);
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        if (myList != null)
//            return myList.get(position).hashCode();
//        return 0;
//
//    }
//
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        Holder holder;
//
//        //If the listview does not have an xml layout ready set the layout
//        if (convertView == null) {
//
//            //we need a new holder to hold the structure of the cell
//            holder = new Holder();
//
//            //get the XML inflation service
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            //Inflate our xml cell to the convertView
//            convertView = inflater.inflate(R.layout.image_signup_adapter, null);
//
//            //Get xml components into our holder class
//            holder.imageView = (ImageView) convertView.findViewById(R.id.car_in_image);
//
//            //Attach our holder class to this particular cell
//            convertView.setTag(holder);
//
//        } else {
//            //The listview cell is not empty and contains already components loaded, get the tagged holder
//            holder = (Holder) convertView.getTag();
//        }
//        //Fill our cell with data
//        //get our person object from the list we passed to the adapter
//        SignUpImageModel person = getItem(position);
////        Picasso.with(context).load(person.getImageUrl()).fit().into(holder.imageView);
//        assert person != null;
//        Bitmap photo = (Bitmap) Objects.requireNonNull(person.getImagePath());
//        holder.imageView.setImageBitmap(photo);
//        return convertView;
//    }
//
//    /**
//     * This holder must replicate the components in the person_cell.xml
//     * We have a textview for the name and the surname and an imageview for the picture
//     */
//    private class Holder {
//        ImageView imageView;
//
//    }
//}