CREATE INDEX idx_otp_unused_user_purpose_created
    ON otp_codes (user_id, purpose, created_at DESC)
    WHERE used = FALSE;