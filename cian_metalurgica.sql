-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versión del servidor:         10.4.32-MariaDB - mariadb.org binary distribution
-- SO del servidor:              Win64
-- HeidiSQL Versión:             12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Volcando estructura de base de datos para cian_metalurgica
CREATE DATABASE IF NOT EXISTS `cian_metalurgica` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `cian_metalurgica`;

-- Volcando estructura para tabla cian_metalurgica.clientes
CREATE TABLE IF NOT EXISTS `clientes` (
  `id_cliente` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre_razon_social` varchar(255) NOT NULL,
  `cuit_dni` varchar(50) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_cliente`),
  KEY `idx_clientes_nombre` (`nombre_razon_social`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla cian_metalurgica.clientes: ~2 rows (aproximadamente)
INSERT IGNORE INTO `clientes` (`id_cliente`, `nombre_razon_social`, `cuit_dni`, `direccion`, `telefono`, `email`) VALUES
	(3, 'Luciano', '214214124', 'lkj14314klj', '124214124', 'lñjdkasfñljdfas'),
	(5, 'GG', '123123123', 'oijpoji123', '123123123', 'oipj123j3');

-- Volcando estructura para tabla cian_metalurgica.detalles
CREATE TABLE IF NOT EXISTS `detalles` (
  `id_detalle` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint(20) NOT NULL,
  `id_material` bigint(20) DEFAULT NULL,
  `material_tipo` varchar(255) DEFAULT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `dimensiones_pieza` varchar(255) DEFAULT NULL,
  `numero_cortes` int(11) DEFAULT NULL,
  `peso_pieza` double DEFAULT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `id_pedido` (`id_pedido`),
  KEY `id_material` (`id_material`),
  CONSTRAINT `detalles_ibfk_1` FOREIGN KEY (`id_pedido`) REFERENCES `pedidos` (`id_pedido`) ON DELETE CASCADE,
  CONSTRAINT `detalles_ibfk_2` FOREIGN KEY (`id_material`) REFERENCES `materiales` (`id_material`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla cian_metalurgica.detalles: ~9 rows (aproximadamente)
INSERT IGNORE INTO `detalles` (`id_detalle`, `id_pedido`, `id_material`, `material_tipo`, `cantidad`, `dimensiones_pieza`, `numero_cortes`, `peso_pieza`) VALUES
	(2, 1, 2, 'ffff', 123, NULL, NULL, NULL),
	(3, 1, 2, 'ffff', 2, NULL, NULL, NULL),
	(4, 1, 1, 'Madera', 2, NULL, NULL, NULL),
	(6, 7, 4, 'Placa', 22, '22', 22, 22),
	(7, 8, 1, 'Madera', 6, '6', 6, 6),
	(8, 9, 2, 'ffff', 32, '23', 32, 23),
	(9, 10, 2, 'ffff', 34, '34', 34, 34),
	(10, 11, 2, 'ffff', 2, NULL, NULL, NULL),
	(11, 12, 3, 'Perfil', 22, '22', 22, 22);

-- Volcando estructura para tabla cian_metalurgica.facturas
CREATE TABLE IF NOT EXISTS `facturas` (
  `id_factura` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint(20) NOT NULL,
  `fecha_emision` date NOT NULL,
  `total` double NOT NULL DEFAULT 0,
  `nro_factura` varchar(100) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_factura`),
  KEY `fk_factura_pedido` (`id_pedido`),
  CONSTRAINT `fk_factura_pedido` FOREIGN KEY (`id_pedido`) REFERENCES `pedidos` (`id_pedido`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Volcando datos para la tabla cian_metalurgica.facturas: ~0 rows (aproximadamente)

-- Volcando estructura para tabla cian_metalurgica.factura_detalles
CREATE TABLE IF NOT EXISTS `factura_detalles` (
  `id_detalle_fact` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_factura` bigint(20) NOT NULL,
  `id_material` bigint(20) DEFAULT NULL,
  `material_tipo` varchar(255) DEFAULT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `cantidad` double DEFAULT NULL,
  `precio_unitario` double DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_detalle_fact`),
  KEY `fk_factura_detalle_factura` (`id_factura`),
  CONSTRAINT `fk_factura_detalle_factura` FOREIGN KEY (`id_factura`) REFERENCES `facturas` (`id_factura`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Volcando datos para la tabla cian_metalurgica.factura_detalles: ~0 rows (aproximadamente)

-- Volcando estructura para tabla cian_metalurgica.materiales
CREATE TABLE IF NOT EXISTS `materiales` (
  `id_material` bigint(20) NOT NULL AUTO_INCREMENT,
  `articulo` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) NOT NULL,
  `espesor` double DEFAULT NULL,
  `dimensiones` varchar(255) DEFAULT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `proveedor` varchar(255) DEFAULT NULL,
  `stock` int(11) DEFAULT 0,
  PRIMARY KEY (`id_material`),
  KEY `idx_materiales_articulo` (`articulo`),
  KEY `idx_materiales_tipo` (`tipo`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla cian_metalurgica.materiales: ~4 rows (aproximadamente)
INSERT IGNORE INTO `materiales` (`id_material`, `articulo`, `tipo`, `espesor`, `dimensiones`, `cantidad`, `proveedor`, `stock`) VALUES
	(1, NULL, 'Madera', 14, '12', 0, 'kñfjaskjafjdfa', 0),
	(2, NULL, 'ffff', 344, '333', 21, 'rewwr', 0),
	(3, 'kjhkljhklj', 'Perfil', NULL, '51', 12, NULL, 0),
	(4, NULL, 'Placa', 12313, '1212', 12, 'weqwe', 0);

-- Volcando estructura para tabla cian_metalurgica.pedidos
CREATE TABLE IF NOT EXISTS `pedidos` (
  `id_pedido` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_cliente` bigint(20) NOT NULL,
  `fecha_pedido` date NOT NULL,
  `fecha_entrega_estimada` date DEFAULT NULL,
  `kilo_cantidad` double DEFAULT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'CREADO',
  `estado_pedido` varchar(50) DEFAULT 'PENDIENTE',
  PRIMARY KEY (`id_pedido`),
  KEY `id_cliente` (`id_cliente`),
  KEY `idx_pedidos_estado` (`estado`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla cian_metalurgica.pedidos: ~9 rows (aproximadamente)
INSERT IGNORE INTO `pedidos` (`id_pedido`, `id_cliente`, `fecha_pedido`, `fecha_entrega_estimada`, `kilo_cantidad`, `estado`, `estado_pedido`) VALUES
	(1, 3, '2025-09-28', '2025-10-25', 34, 'CREADO', 'PENDIENTE'),
	(5, 3, '2025-10-07', NULL, 13, 'CREADO', 'PENDIENTE'),
	(6, 3, '2025-10-07', NULL, 23, 'CREADO', 'PENDIENTE'),
	(7, 3, '2025-10-07', NULL, NULL, 'CREADO', 'PENDIENTE'),
	(8, 3, '2025-10-08', NULL, NULL, 'CREADO', 'PENDIENTE'),
	(9, 5, '2025-10-07', NULL, NULL, 'CREADO', 'PENDIENTE'),
	(10, 5, '2025-10-07', NULL, NULL, 'CREADO', 'PENDIENTE'),
	(11, 5, '2025-10-07', NULL, NULL, 'CREADO', 'PENDIENTE'),
	(12, 5, '2025-10-07', NULL, NULL, 'CREADO', 'PENDIENTE');

-- Volcando estructura para tabla cian_metalurgica.stock_movimientos
CREATE TABLE IF NOT EXISTS `stock_movimientos` (
  `id_movimiento` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_material` bigint(20) NOT NULL,
  `fecha_movimiento` timestamp NOT NULL DEFAULT current_timestamp(),
  `tipo_movimiento` enum('SALIDA','ENTRADA','AJUSTE') NOT NULL,
  `cantidad` double NOT NULL,
  `referencia` varchar(255) DEFAULT NULL,
  `comentario` text DEFAULT NULL,
  `usuario` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `fk_stock_material` (`id_material`),
  CONSTRAINT `fk_stock_material` FOREIGN KEY (`id_material`) REFERENCES `materiales` (`id_material`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Volcando datos para la tabla cian_metalurgica.stock_movimientos: ~0 rows (aproximadamente)

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
