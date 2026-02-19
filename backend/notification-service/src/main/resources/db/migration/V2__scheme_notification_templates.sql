-- Seed templates for scheme-service events
-- Language: en

-- APPLICATION_SUBMITTED
INSERT INTO notification_templates (code, channel, language, title, body, created_at, updated_at)
VALUES
  ('SCHEME_APPLICATION_SUBMITTED', 'SMS', 'en', 'Scheme Application Submitted',
   'Your application (${applicationId}) for scheme (${schemeId}) has been submitted.', now(), now()),
  ('SCHEME_APPLICATION_SUBMITTED', 'WHATSAPP', 'en', 'Scheme Application Submitted',
   'Your application (${applicationId}) for scheme (${schemeId}) has been submitted.', now(), now())
ON CONFLICT (code, channel, language) DO NOTHING;


-- APPLICATION_APPROVED
INSERT INTO notification_templates (code, channel, language, title, body, created_at, updated_at)
VALUES
  ('SCHEME_APPLICATION_APPROVED', 'SMS', 'en', 'Scheme Application Approved',
   'Good news! Your application (${applicationId}) for scheme (${schemeId}) has been approved.', now(), now()),
  ('SCHEME_APPLICATION_APPROVED', 'WHATSAPP', 'en', 'Scheme Application Approved',
   'Good news! Your application (${applicationId}) for scheme (${schemeId}) has been approved.', now(), now())
ON CONFLICT (code, channel, language) DO NOTHING;


-- APPLICATION_REJECTED
INSERT INTO notification_templates (code, channel, language, title, body, created_at, updated_at)
VALUES
  ('SCHEME_APPLICATION_REJECTED', 'SMS', 'en', 'Scheme Application Rejected',
   'Your application (${applicationId}) for scheme (${schemeId}) was rejected. ${reason}', now(), now()),
  ('SCHEME_APPLICATION_REJECTED', 'WHATSAPP', 'en', 'Scheme Application Rejected',
   'Your application (${applicationId}) for scheme (${schemeId}) was rejected. ${reason}', now(), now())
ON CONFLICT (code, channel, language) DO NOTHING;


-- NEED_MORE_INFO
INSERT INTO notification_templates (code, channel, language, title, body, created_at, updated_at)
VALUES
  ('SCHEME_APPLICATION_NEED_MORE_INFO', 'SMS', 'en', 'More Information Needed',
   'More information is needed for your application (${applicationId}) for scheme (${schemeId}). ${reason}', now(), now()),
  ('SCHEME_APPLICATION_NEED_MORE_INFO', 'WHATSAPP', 'en', 'More Information Needed',
   'More information is needed for your application (${applicationId}) for scheme (${schemeId}). ${reason}', now(), now())
ON CONFLICT (code, channel, language) DO NOTHING;


-- APPLICATION_CANCELLED
INSERT INTO notification_templates (code, channel, language, title, body, created_at, updated_at)
VALUES
  ('SCHEME_APPLICATION_CANCELLED', 'SMS', 'en', 'Scheme Application Cancelled',
   'Your application (${applicationId}) for scheme (${schemeId}) was cancelled. ${reason}', now(), now()),
  ('SCHEME_APPLICATION_CANCELLED', 'WHATSAPP', 'en', 'Scheme Application Cancelled',
   'Your application (${applicationId}) for scheme (${schemeId}) was cancelled. ${reason}', now(), now())
ON CONFLICT (code, channel, language) DO NOTHING;

