package com.lisss79.android.myperiodictasks;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TasksListAdapter extends RecyclerView.Adapter<TasksListAdapter.TasksViewHolder> {
    private ArrayList<String> mTasksList;
    private ArrayList<LocalDate> mDateList;
    private ArrayList<Boolean> isActive;
    public ArrayList<Boolean> mItemSelected = new ArrayList<>(); // false - не выбрана, true - выбрана
    private LayoutInflater mInflater;
    private Context context;
    public boolean usualMode = true; // true - обычный режим, false - режим выбора
    private int posOnLongClick = 0; // элемент, по которому выполнен long click
    public boolean selAll; // активизация пункта "выбор всех"
    public boolean unSelAll; // активизация отмены выбора
    //LoadAndSaveData loadAndSaveData; // класс с методами загрузки/сохранения
    ObjectAnimator animation1, animation2, animation3; // объекты анимации элементов
    DateTimeFormatter formatter; // формат даты для показа

    public TasksListAdapter(Context context, ArrayList<String> tasksList,
                            ArrayList<LocalDate> dateList, ArrayList<Boolean> isActive) {
        mInflater = LayoutInflater.from(context);
        this.mTasksList = tasksList;
        this.mDateList = dateList;
        this.isActive = isActive;
        this.context = context;
        formatter = ((MainActivity) context).formatter;
        //loadAndSaveData = new LoadAndSaveData(context, context.getFilesDir());
    }

    @Override
    public void onBindViewHolder(
            @NonNull TasksListAdapter.TasksViewHolder holder, int position) {
        String mCurrentName = mTasksList.get(position);
        LocalDate mCurrentDate = mDateList.get(position);
        String mDateText = mCurrentDate.format(formatter);
        Boolean mCurrentIsActive = isActive.get(position);

        // режим выбора элементов - показать checkbox
        if (!usualMode) {
            // отметить галочкой текущий элемент и добавить в список выбора,
            // кроме случая отмены выбора
            // или выбирать все, если выбран соответствующий пункт меню
            if (((position == posOnLongClick) && !unSelAll) || selAll ||
                    (mItemSelected.get(position) && !unSelAll)) {
                holder.selectedCheckBox.setChecked(true);
                mItemSelected.set(position, true);
                if (position == posOnLongClick)
                    posOnLongClick = -1; // уже выбрали задачу после long click
            }
            // убрать галочку с остальных элементов и убрать их из списка выбора,
            // кроме случая "выбрать все"
            // убирать все, если выбран соответствующий пункт меню
            else if (unSelAll || !mItemSelected.get(position)) {
                holder.selectedCheckBox.setChecked(false);
                mItemSelected.set(position, false);
            }

            //TextView textView = holder.name_of_tasksItemView;
            //Animation hyperspaceJump = AnimationUtils.loadAnimation(context, R.anim.move_right);
            //textView.startAnimation(hyperspaceJump);
            animation1 = ObjectAnimator.ofFloat(holder.name_of_tasksItemView, "translationX", 100f);
            animation2 = ObjectAnimator.ofFloat(holder.date_of_tasksItemView, "translationX", 100f);
            animation3 = ObjectAnimator.ofFloat(holder.selectedCheckBox, "Alpha", 0f, 1f);
            animation1.setDuration(50);
            animation2.setDuration(50);
            animation3.setDuration(150);
            animation1.start();
            animation2.start();
            animation3.start();

            holder.selectedCheckBox.setVisibility(View.VISIBLE);
            holder.selectedCheckBox.setSelected(false);

        }
        // режим просмотра элементов - убрать checkbox
        else {

            if (holder.selectedCheckBox.getVisibility() == View.VISIBLE) {
                animation1 = ObjectAnimator.ofFloat(holder.name_of_tasksItemView, "translationX", 0f);
                animation2 = ObjectAnimator.ofFloat(holder.date_of_tasksItemView, "translationX", 0f);
                animation1.setDuration(50);
                animation2.setDuration(50);
                animation1.start();
                animation2.start();
            }

            holder.selectedCheckBox.setVisibility(View.GONE);
            holder.selectedCheckBox.setChecked(false);
            mItemSelected.set(position, false);
        }
        holder.name_of_tasksItemView.setText(mCurrentName);
        holder.date_of_tasksItemView.setText(mDateText);
        holder.tasksSwitchCompat.setChecked(mCurrentIsActive);

    }

    @Override
    public TasksListAdapter.TasksViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.tasks_item, parent, false);
        return new TasksViewHolder(mItemView, this, context);
    }


    @Override
    public int getItemCount() {
        return mTasksList.size();
    }

    // очистить массив выбора задач с инициализацией или без
    public void clearSelection(boolean init) {
        if (init) {
            // инициализация массива выбора
            for (int j = 0; j < mTasksList.size(); j++) {
                mItemSelected.add(false);
            }
        }
        // очистка массива выбора
        for (int i = 0; i < mTasksList.size(); i++) {
            mItemSelected.set(i, false);
        }
    }

    public static class TasksViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public final TextView name_of_tasksItemView;
        public final TextView date_of_tasksItemView;
        public final CheckBox selectedCheckBox;
        public final CardView tasksCardView;
        public final RelativeLayout relativeLayout;
        public final androidx.appcompat.widget.SwitchCompat tasksSwitchCompat;
        final TasksListAdapter mAdapter;
        ActivityResultLauncher<Intent> someActivityResultLauncher;
        Context context;

        public TasksViewHolder(View itemView, TasksListAdapter adapter, Context context) {
            super(itemView);
            name_of_tasksItemView = itemView.findViewById(R.id.name_of_tasks_textView);
            date_of_tasksItemView = itemView.findViewById(R.id.next_date_of_tasks_textView);
            selectedCheckBox = itemView.findViewById(R.id.selected_checkBox);
            tasksCardView = itemView.findViewById(R.id.tasks_card_view);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            tasksSwitchCompat = itemView.findViewById(R.id.task_switch_compat);
            this.mAdapter = adapter;
            this.context = context;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            selectedCheckBox.setOnClickListener(onSelClick);
            tasksSwitchCompat.setOnClickListener(onSwitchTask);

        }

        // обработка нажатия на задачу
        @Override
        public void onClick(View view) {
            mAdapter.selAll = false;
            mAdapter.unSelAll = false;
            int position = getLayoutPosition();
            if (mAdapter.usualMode) {
                ((MainActivity) context).showDetails(position, false);
            } else {
                //mAdapter.clearSelection();
                ((MainActivity) context).showDetails(position, false);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            mAdapter.selAll = false;
            mAdapter.unSelAll = false;
            mAdapter.posOnLongClick = getLayoutPosition();
            mAdapter.usualMode = !mAdapter.usualMode;
            mAdapter.clearSelection(false);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        // обработка нажатия на checkbox в режиме выбора
        private View.OnClickListener onSelClick = new View.OnClickListener() {
            public void onClick(View v) {
                mAdapter.selAll = false;
                mAdapter.unSelAll = false;
                int position = getLayoutPosition();
                mAdapter.mItemSelected.set(position, ((CheckBox) v).isChecked());
            }
        };

        // обработка нажатия на switch
        private View.OnClickListener onSwitchTask = new View.OnClickListener() {
            public void onClick(View v) {
                int position = getLayoutPosition();
                int newPosition = position;
                if (tasksSwitchCompat.isChecked()) {
                    Toast.makeText(context, "Задача активна", Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).isActive.set(position, true);
                    newPosition = goUp(position);
                } else {
                    Toast.makeText(context, "Задача отключена", Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).isActive.set(position, false);
                    newPosition = goDown(position);
                }
                // переместить элемент, если нужно
                if(newPosition != position) {
                    String task = ((MainActivity) context).mTasksList.get(position);
                    ((MainActivity) context).mTasksList.remove(position);
                    LocalDate date = ((MainActivity) context).mDateList.get(position);
                    ((MainActivity) context).mDateList.remove(position);
                    Integer period = ((MainActivity) context).mPeriodList.get(position);
                    ((MainActivity) context).mPeriodList.remove(position);
                    Boolean lengthPeriod = ((MainActivity) context).mLengthPeriodList.get(position);
                    ((MainActivity) context).mLengthPeriodList.remove(position);
                    Boolean isActive = ((MainActivity) context).isActive.get(position);
                    ((MainActivity) context).isActive.remove(position);
                    mAdapter.notifyItemRemoved(position);

                    ((MainActivity) context).mTasksList.add(newPosition, task);
                    ((MainActivity) context).mDateList.add(newPosition, date);
                    ((MainActivity) context).mPeriodList.add(newPosition, period);
                    ((MainActivity) context).mLengthPeriodList.add(newPosition, lengthPeriod);
                    ((MainActivity) context).isActive.add(newPosition, isActive);
                    mAdapter.notifyItemInserted(newPosition);

                }
                ((MainActivity) context).saveData();
            }
        };

        // перемещение активизированной задачи вверх по списку, возвращает новую позицию
        private int goUp(int currPos) {
            int newPos = currPos;
            LocalDate date1, date2;
            boolean isActive2;
            date1 = ((MainActivity) context).mDateList.get(currPos);
            for(int i = currPos; i >= 0; i--) {
                date2 = ((MainActivity) context).mDateList.get(i);
                isActive2 = ((MainActivity) context).isActive.get(i);
                if((isActive2 && date1.isBefore(date2)) || !isActive2){
                    newPos = i;
                }
            }
            return newPos;
        }

        // перемещение отключенной задачи вниз по списку, возвращает новую позицию
        private int goDown(int currPos) {
            int newPos = currPos;
            LocalDate date1, date2;
            boolean isActive1, isActive2;
            date1 = ((MainActivity) context).mDateList.get(currPos);
            isActive1 = ((MainActivity) context).isActive.get(currPos);
            for(int i = currPos; i < ((MainActivity) context).mTasksList.size(); i++) {
                date2 = ((MainActivity) context).mDateList.get(i);
                isActive2 = ((MainActivity) context).isActive.get(i);
                if((!isActive2 && date1.isAfter(date2)) || isActive2){
                    newPos = i;
                }
            }
            return newPos;
        }

    }
}
