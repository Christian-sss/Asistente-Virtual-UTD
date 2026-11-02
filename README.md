# Asistente Virtual de Trámites Documentarios – UTD UNJBG

Esta aplicación web nace de una necesidad concreta de la Unidad de Trámite Documentario de la Universidad Nacional Jorge Basadre Grohmann: que las personas lleguen a la ventanilla sabiendo qué trámite les corresponde, cuánto deben pagar, qué requisitos presentar y en qué oficina hacerlo. En lugar de repartir esa información en avisos, carteles o consultas sueltas, el sistema la concentra y la convierte en una guía que se puede seguir de principio a fin.

## El problema

Cuando alguien necesita una constancia, un certificado o un trámite de matrícula, lo habitual es encontrarse con información dispersa y con la duda de si el documento está completo. Muchas solicitudes se devuelven por datos que faltan, pagos que no se adjuntan o requisitos que no quedaron claros. Cada observación significa volver, rehacer el papeleo y esperar más.

El asistente busca reducir eso: ordena la información oficial, ayuda a identificar el trámite correcto y permite revisar la solicitud antes de presentarla.

## Qué ofrece

Acompaña al usuario en cuatro momentos:

- **Encontrar el trámite.** Un catálogo con los procedimientos vigentes, sus requisitos, montos, plazos y oficina de atención, con búsqueda por categoría o palabra clave.
- **Identificarlo cuando no sabe cómo se llama.** A través de preguntas simples, el sistema conduce a la opción que corresponde.
- **Prepararlo.** Genera una hoja de ruta con los pasos, el pago previo y el sustento normativo de cada requisito, disponible en pantalla y para descargar en PDF.
- **Revisarlo antes de entregarlo.** Permite cargar la solicitud en PDF, JPG o PNG y hace una revisión preliminar de formato, tamaño, páginas, datos básicos y seguridad del archivo. El resultado se muestra criterio por criterio —cumple, observado o no verificable— junto con una confirmación asistida de firma y anexos.

También incluye un espacio para consultar la normativa que respalda cada requisito y un panel para que el personal mantenga actualizado el contenido (trámites, requisitos, reglas de revisión, normativa y preguntas guía), dejando registro de cada cambio. Los documentos temporales se eliminan solos cuando vence su plazo de conservación.

## De dónde sale la información

Los trámites, requisitos, montos y plazos provienen del TUPA vigente de la UNJBG, aprobado por la R.R. N.° 11021-2023-UNJBG.

## Cómo está pensado

Está construido como una aplicación web en capas: las vistas se apoyan en controladores que delegan la lógica en servicios, y estos acceden a una base de datos relacional. Esa separación permite mantener el contenido y las reglas de revisión sin tocar la interfaz, y deja preparado el sistema para crecer con nuevos trámites y requisitos.
