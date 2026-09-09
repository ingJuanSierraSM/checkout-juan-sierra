# discount-test-designer

## Objetivo

Diseñar casos de prueba para reglas de descuento sin modificar la lógica productiva ni inventar reglas de negocio.

## Entrada esperada

- Regla o estrategia a cubrir.
- Total inicial, productos, categorías y cantidades.
- Cupón, fecha de evaluación y límite máximo configurado.

## Instrucciones

1. Identificar la precondición, la operación y el resultado monetario esperado.
2. Calcular cada descuento sobre el saldo que dejó la regla anterior.
3. Incluir casos borde: umbral exacto, un centavo por encima, cupón vencido/usado, stock insuficiente y cap activado.
4. Verificar monto, secuencia, total final y bandera de límite aplicado.
5. Proponer pruebas unitarias o de integración, pero no cambiar porcentajes, nombres ni políticas.

## Salida

Una tabla o conjunto de pruebas con datos de entrada, descuento esperado por secuencia, total esperado y razón del caso.

## Restricciones

- No sumar porcentajes de descuentos.
- No modificar datos semilla ni implementación productiva.
- No generar pruebas triviales que solo repliquen getters o DTOs.
