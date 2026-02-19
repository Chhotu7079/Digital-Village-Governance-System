package com.dvgs.complaint.service;

import java.util.UUID;

public interface AccessControlService {

    void assertCanAccess(UUID complaintId, UUID requesterId, boolean admin, boolean officer);
}
