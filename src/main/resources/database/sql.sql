-- 사용자 목록
CREATE TABLE app_user (
  id BIGSERIAL PRIMARY KEY,
  provider_uid VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 루틴 정보 및 목록
CREATE TABLE routine (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  title VARCHAR(100) NOT NULL,
  goal TEXT,
  time TIME NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  color VARCHAR(7),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  is_notify BOOLEAN,

  CONSTRAINT fk_routine_user
    FOREIGN KEY (user_id) REFERENCES app_user(provider_uid)
    ON DELETE CASCADE
);

-- 루틴 요일 반복 패턴
CREATE TABLE routine_day (
  id BIGSERIAL PRIMARY KEY,
  routine_id BIGINT NOT NULL,
  day_of_week SMALLINT NOT NULL,

  CONSTRAINT fk_routine_day_routine
    FOREIGN KEY (routine_id) REFERENCES routine(id)
    ON DELETE CASCADE,

  CONSTRAINT uq_routine_day
    UNIQUE (routine_id, day_of_week)
);

-- 루틴 수행 여부
CREATE TABLE routine_log (
  id BIGSERIAL PRIMARY KEY,
  routine_id BIGINT NOT NULL,
  log_date DATE NOT NULL,
  status VARCHAR(10) NOT NULL,
  checked_at TIMESTAMP NOT NULL DEFAULT NOW(),

  CONSTRAINT fk_routine_log_routine
    FOREIGN KEY (routine_id) REFERENCES routine(id)
    ON DELETE CASCADE,

  CONSTRAINT uq_routine_log
    UNIQUE (routine_id, log_date),

  CONSTRAINT chk_routine_log_status
    CHECK (status IN ('DONE', 'MISS'))
);

