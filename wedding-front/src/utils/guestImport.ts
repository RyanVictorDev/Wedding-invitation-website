import * as XLSX from 'xlsx'

export interface ParsedGuestRow {
  rowNumber: number
  name: string
  godparent: boolean | null
  errors: string[]
}

const TEMPLATE_HEADERS = ['Nome', 'Padrinho/Madrinha']

function toText (value: unknown): string {
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'bigint') {
    return String(value)
  }
  if (value instanceof Date) return value.toISOString()
  return ''
}

export function downloadGuestTemplate () {
  const worksheet = XLSX.utils.aoa_to_sheet([
    TEMPLATE_HEADERS,
    ['', '']
  ])
  worksheet['!cols'] = [{ wch: 42 }, { wch: 20 }]

  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Convidados')
  XLSX.writeFile(workbook, 'modelo-convidados.xlsx')
}

function parseGodparent (value: unknown): { godparent: boolean | null; error?: string } {
  const text = toText(value).trim()

  if (!text) {
    return { godparent: null, error: 'Padrinho/Madrinha é obrigatório' }
  }

  const normalized = text.toLowerCase()

  if (['sim', 's', 'true', '1', 'yes'].includes(normalized)) {
    return { godparent: true }
  }

  if (['nao', 'não', 'n', 'false', '0', 'no'].includes(normalized)) {
    return { godparent: false }
  }

  return { godparent: null, error: 'Use Sim ou Não em Padrinho/Madrinha' }
}

function normalizeHeader (value: unknown) {
  return toText(value).trim().toLowerCase()
}

export function parseGuestSpreadsheet (file: ArrayBuffer): { rows: ParsedGuestRow[]; fileError?: string } {
  const workbook = XLSX.read(file, { type: 'array' })
  const sheetName = workbook.SheetNames[0]

  if (!sheetName) {
    return { rows: [], fileError: 'Planilha vazia.' }
  }

  const sheet = workbook.Sheets[sheetName]

  if (!sheet) {
    return { rows: [], fileError: 'Planilha vazia.' }
  }

  const matrix = XLSX.utils.sheet_to_json<unknown[]>(sheet, { header: 1, defval: '' })

  if (!matrix.length) {
    return { rows: [], fileError: 'Planilha vazia.' }
  }

  const headerRow = matrix[0]

  if (!headerRow) {
    return { rows: [], fileError: 'Planilha vazia.' }
  }

  const headers = headerRow.map(normalizeHeader)
  const expected = TEMPLATE_HEADERS.map(normalizeHeader)

  const headersValid = expected.every((header, index) => headers[index] === header)

  if (!headersValid) {
    return {
      rows: [],
      fileError: 'Cabeçalhos inválidos. Baixe o modelo e use as colunas Nome e Padrinho/Madrinha.'
    }
  }

  const rows: ParsedGuestRow[] = []
  const seenNames = new Map<string, number>()

  for (let index = 1; index < matrix.length; index++) {
    const cells = matrix[index] ?? []
    const nameRaw = toText(cells[0]).trim()
    const godparentRaw = cells[1]
    const rowNumber = index + 1

    if (!nameRaw && !toText(godparentRaw).trim()) {
      continue
    }

    const errors: string[] = []
    let godparent: boolean | null = null

    if (!nameRaw) {
      errors.push('Nome é obrigatório')
    } else if (nameRaw.length > 150) {
      errors.push('Nome deve ter no máximo 150 caracteres')
    }

    const parsedGodparent = parseGodparent(godparentRaw)
    if (parsedGodparent.error) {
      errors.push(parsedGodparent.error)
    } else {
      godparent = parsedGodparent.godparent
    }

    if (nameRaw) {
      const key = nameRaw.toLowerCase()
      const duplicateRow = seenNames.get(key)

      if (duplicateRow != null) {
        errors.push(`Nome duplicado na planilha (linha ${duplicateRow})`)
      } else {
        seenNames.set(key, rowNumber)
      }
    }

    rows.push({
      rowNumber,
      name: nameRaw,
      godparent,
      errors
    })
  }

  if (!rows.length) {
    return { rows: [], fileError: 'Nenhum convidado encontrado na planilha.' }
  }

  return { rows }
}

export function isImportValid (rows: ParsedGuestRow[]) {
  return rows.length > 0 && rows.every(row => row.errors.length === 0)
}
