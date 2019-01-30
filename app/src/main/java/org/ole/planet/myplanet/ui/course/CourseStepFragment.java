package org.ole.planet.myplanet.ui.course;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ole.planet.myplanet.R;
import org.ole.planet.myplanet.base.BaseContainerFragment;
import org.ole.planet.myplanet.datamanager.DatabaseService;
import org.ole.planet.myplanet.model.RealmCourseProgress;
import org.ole.planet.myplanet.model.RealmCourseStep;
import org.ole.planet.myplanet.model.RealmMyCourse;
import org.ole.planet.myplanet.model.RealmMyLibrary;
import org.ole.planet.myplanet.model.RealmStepExam;
import org.ole.planet.myplanet.model.RealmUserModel;
import org.ole.planet.myplanet.service.UserProfileDbHandler;
import org.ole.planet.myplanet.ui.exam.TakeExamFragment;
import org.ole.planet.myplanet.utilities.Constants;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import br.tiagohm.markdownview.MarkdownView;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class CourseStepFragment extends BaseContainerFragment {

    TextView tvTitle;
    MarkdownView description;
    String stepId;
    Button btnResource, btnExam, btnOpen;
    DatabaseService dbService;
    Realm mRealm;
    RealmCourseStep step;
    List<RealmMyLibrary> resources;
    List<RealmStepExam> stepExams;
    RealmUserModel user;
    int stepNumber;

    public CourseStepFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepId = getArguments().getString("stepId");
            stepNumber = getArguments().getInt("stepNumber");
        }
        setUserVisibleHint(false);
    }

    @Override
    public void playVideo(String videoType, RealmMyLibrary items) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_step, container, false);
        tvTitle = v.findViewById(R.id.tv_title);
        description = v.findViewById(R.id.description);
        btnExam = v.findViewById(R.id.btn_take_test);
        btnOpen = v.findViewById(R.id.btn_open);
        btnResource = v.findViewById(R.id.btn_resources);
        dbService = new DatabaseService(getActivity());
        mRealm = dbService.getRealmInstance();
        user = new UserProfileDbHandler(getActivity()).getUserModel();
        btnExam.setVisibility(Constants.showBetaFeature(Constants.KEY_EXAM, getActivity()) ? View.VISIBLE : View.GONE);
        return v;
    }

    public void saveCourseProgress() {
        if (!mRealm.isInTransaction())
            mRealm.beginTransaction();
        RealmCourseProgress courseProgress = mRealm.where(RealmCourseProgress.class)
                .equalTo("courseId", step.getCourseId())
                .equalTo("userId", user.getId())
                .equalTo("stepNum", stepNumber)
                .findFirst();
        if (courseProgress == null) {
            courseProgress = mRealm.createObject(RealmCourseProgress.class, UUID.randomUUID().toString());
        }
        courseProgress.setCourseId(step.getCourseId());
        courseProgress.setStepNum(stepNumber);
        courseProgress.setPassed(stepExams.size() <= 0);
        courseProgress.setCreatedOn(new Date().getTime());
        courseProgress.setParentCode(user.getParentCode());
        courseProgress.setUserId(user.getId());
        mRealm.commitTransaction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        step = mRealm.where(RealmCourseStep.class).equalTo("id", stepId).findFirst();
        resources = mRealm.where(RealmMyLibrary.class).equalTo("stepId", stepId).findAll();
        stepExams = mRealm.where(RealmStepExam.class).equalTo("stepId", stepId).findAll();
        if (resources != null)
            btnResource.setText("Resources [" + resources.size() + "]");
        if (stepExams != null)
            btnExam.setText("Take Test [" + stepExams.size() + "]");
        tvTitle.setText(step.getStepTitle());
        description.loadMarkdown(step.getDescription());
        setListeners();

    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && RealmMyCourse.isMyCourse(user.getId(), step.getCourseId(), mRealm)) {
            saveCourseProgress();
        }
    }

    private void setListeners() {
        final RealmResults offlineResources = mRealm.where(RealmMyLibrary.class)
                .equalTo("stepId", stepId)
                .equalTo("resourceOffline", false)
                .isNotNull("resourceLocalAddress")
                .findAll();
        setResourceButton(offlineResources, btnResource);

        btnExam.setOnClickListener(view -> {
            if (stepExams.size() > 0) {
                Fragment takeExam = new TakeExamFragment();
                Bundle b = new Bundle();
                b.putString("stepId", stepId);
                b.putInt("stepNum", stepNumber);
                takeExam.setArguments(b);
                homeItemClickListener.openCallFragment(takeExam);
            }
        });
        final List<RealmMyLibrary> downloadedResources = mRealm.where(RealmMyLibrary.class)
                .equalTo("stepId", stepId)
                .equalTo("resourceOffline", true)
                .isNotNull("resourceLocalAddress")
                .findAll();

        setOpenResourceButton(downloadedResources, btnOpen);

    }

    @Override
    public void onDownloadComplete() {
        super.onDownloadComplete();
        setListeners();
    }
}