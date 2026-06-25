-- =============================================
-- Script para insertar usuarios de prueba
-- Password para ambos: Admin123!
-- =============================================

INSERT INTO users (id, name, email, password, role, state, created_at)
VALUES
(
    gen_random_uuid(),
    'Admin TourismSV',
    'admin@tourismsv.com',
    '$2a$12$imOdw5EgmHrQ/V0U5Pu0j.eRk3w2hJ6FJdjuYZppuj86xH/d3F/am',
    'ADMIN',
    'ACTIVE',
    NOW()
),
(
    gen_random_uuid(),
    'Tourist de Prueba',
    'tourist@tourismsv.com',
    '$2a$12$imOdw5EgmHrQ/V0U5Pu0j.eRk3w2hJ6FJdjuYZppuj86xH/d3F/am',
    'TOURIST',
    'ACTIVE',
    NOW()
);
