package kz.nurbekov.restaurantserver.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kz.nurbekov.restaurantserver.R;
import kz.nurbekov.restaurantserver.model.Order;

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView name, quantity, price, discount;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.product_name);
        quantity = (TextView) itemView.findViewById(R.id.product_quantity);
        price = (TextView) itemView.findViewById(R.id.product_price);
        discount = (TextView) itemView.findViewById(R.id.product_discount);
    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_layout, parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order order = myOrders.get(position);
        holder.name.setText(String.format("Название : %s", order.getProductName()));
        holder.quantity.setText(String.format("Колиество : %s", order.getQuantity()));
        holder.price.setText(String.format("Цена : %s", order.getPrice()));
        holder.discount.setText(String.format("Скидка : %s", order.getDiscount()));

    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
