module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        'type-enum': [
            2,
            'always',
            [
                'feat',     // Nueva funcionalidad
                'fix',      // Corrección de bug
                'docs',     // Cambios en documentación
                'style',    // Formato, sin cambios de lógica
                'refactor', // Cambio de código sin alterar comportamiento
                'test',     // Agregar o corregir tests
                'chore',    // Tareas de mantenimiento, configuración
                'perf',     // Mejora de rendimiento
                'ci',       // Cambios en CI/CD
                'build',    // Cambios en el sistema de build o dependencias
            ],
        ],
        'subject-case': [0], // Permite acentos y mayúsculas propias del español
        'header-max-length': [2, 'always', 100],
    },
};