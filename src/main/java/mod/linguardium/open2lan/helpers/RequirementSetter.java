package mod.linguardium.open2lan.helpers;

import java.util.function.Predicate;

public interface RequirementSetter {
    void setRequirement(Predicate<?> newRequirement);
}
