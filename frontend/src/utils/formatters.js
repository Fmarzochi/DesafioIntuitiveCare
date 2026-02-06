// src/utils/formatters.js

export function formatarTexto(texto) {
  // Se o texto for vazio ou nulo, retorna vazio para não dar erro
  if (!texto) return '';

  // Lista de palavras que devem ficar minúsculas (preposições)
  const minusculas = ['de', 'da', 'do', 'das', 'dos', 'em', 'na', 'no', 'nas', 'nos', 'por', 'para', 'com', 'e', 'ou', 'a', 'o', 'as', 'os'];

  // Lista de siglas que devem ficar SEMPRE maiúsculas
  const siglas = ['SUS', 'ANS', 'PEONA', 'RN', 'CNPJ', 'CPF', 'ME', 'EPP', 'LTDA', 'SA', 'S.A.'];

  // 1. Transforma tudo em minúsculo e divide por espaços
  const palavras = texto.toLowerCase().split(' ');

  // 2. Analisa palavra por palavra
  const palavrasFormatadas = palavras.map((palavra, index) => {
    // Limpa pontuação para verificar se é sigla (ex: "(SUS)" vira "SUS")
    const palavraLimpa = palavra.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();

    // REGRA A: Se for sigla conhecida, retorna em MAIÚSCULO
    if (siglas.includes(palavraLimpa)) {
        return palavra.toUpperCase();
    }

    // REGRA B: Se for preposição e não for a primeira palavra da frase, mantém minúscula
    if (minusculas.includes(palavra) && index !== 0) {
      return palavra;
    }

    // REGRA C: Caso contrário, apenas a Primeira Letra Maiúscula
    return palavra.charAt(0).toUpperCase() + palavra.slice(1);
  });

  // 3. Junta tudo de volta em uma frase
  return palavrasFormatadas.join(' ');
}