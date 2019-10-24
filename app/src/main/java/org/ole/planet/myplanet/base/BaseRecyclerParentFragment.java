package org.ole.planet.myplanet.base;

import org.ole.planet.myplanet.model.RealmMyCourse;
import org.ole.planet.myplanet.model.RealmMyLibrary;
import org.ole.planet.myplanet.model.RealmStepExam;

import java.util.List;

public class BaseRecyclerParentFragment<LI> extends BaseResourceFragment {
    public boolean isMyCourseLib;


    public List<LI> getList(Class c) {
        if (c == RealmStepExam.class) {
            return mRealm.where(c).equalTo("type", "surveys").findAll();
        } else if (isMyCourseLib) {
            return getMyLibItems(c);
        } else {
            return c == RealmMyLibrary.class ? RealmMyLibrary.getOurLibrary(model.getId(), mRealm.where(c).findAll()) : RealmMyCourse.getOurCourse(model.getId(), mRealm.where(c).findAll());
        }
    }

    public List<LI> getList(Class c, String orderBy) {
        if (c == RealmStepExam.class) {
            return mRealm.where(c).sort(orderBy).equalTo("type", "surveys").findAll();
        } else if (isMyCourseLib) {
            return getMyLibItems(c,orderBy);
        } else {
            return c == RealmMyLibrary.class ? RealmMyLibrary.getOurLibrary(model.getId(), mRealm.where(c).sort(orderBy).findAll()) : RealmMyCourse.getOurCourse(model.getId(), mRealm.where(c).sort(orderBy).findAll());
        }
    }

    private List<LI> getMyLibItems(Class c) {
        if (c == RealmMyLibrary.class)
            return RealmMyLibrary.getMyLibraryByUserId(model.getId(), mRealm.where(c).findAll());
        else
            return RealmMyCourse.getMyCourseByUserId(model.getId(), mRealm.where(c).findAll());
    }

    private List<LI> getMyLibItems(Class c, String orderBy) {
        if (c == RealmMyLibrary.class)
            return RealmMyLibrary.getMyLibraryByUserId(model.getId(), mRealm.where(c).sort(orderBy).findAll());
        else
            return RealmMyCourse.getMyCourseByUserId(model.getId(), mRealm.where(c).sort(orderBy).findAll());
    }
}